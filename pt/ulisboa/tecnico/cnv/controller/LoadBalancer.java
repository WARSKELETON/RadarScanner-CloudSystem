package pt.ulisboa.tecnico.cnv.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.lang.InterruptedException;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import pt.ulisboa.tecnico.cnv.solver.Solver;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;
import pt.ulisboa.tecnico.cnv.server.ServerArgumentParser;
import pt.ulisboa.tecnico.cnv.server.Request;
import pt.ulisboa.tecnico.cnv.server.WebServer;

import javax.imageio.ImageIO;

public class LoadBalancer {

    public static final int HEALTH_CHECK_PERIOD = 30000;
    public static final int HEALTH_CHECK_TIMEOUT = 10000;
    public static final int HEALTH_CHECK_THESHOLD = 2;

    public static final long MAX_WORKLOAD = 500000000;
    public static final int REQUEST_TIMEOUT = 300000;
    public static final int GRACE_PERIOD = 60000;
    public static final int MAX_REQUESTS = 3;
    private static final String LOCAL_IP = "0.0.0.0";

    public LoadBalancer () {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(LOCAL_IP, 8000), 0);
        } catch (Exception e) {
            System.err.println("Failed to launch load balancer " + e.getMessage());
        }

        if (server == null) return;

        server.createContext("/scan", new MyScanHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println(server.getAddress().toString());

        performHealthCheck();
    }

    static class MyScanHandler implements HttpHandler {
        @Override
        public void handle (final HttpExchange t) throws IOException {
            int failedRequests = 0;
            long estimateWorkload = MAX_WORKLOAD;

            final String originalQuery = t.getRequestURI().getQuery();
            String estimateQueryRequest = originalQuery + "&c=false";
            WorkerNode worker = Server.getLaziestWorkerNode();
            if (worker == null) {
                System.out.println("LoadBalancer found no available worker nodes requesting urgent scale up...");
                Server.requestScaleUp();
                try {
                    Thread.sleep(GRACE_PERIOD);
                } catch (InterruptedException e) {
                    System.out.println("LoadBalancer caught exception");
                }
                while (worker == null) {
                    worker = Server.getLaziestWorkerNode();
                }
            }
            String workerIp = worker.getInstance().getPublicIpAddress();

            System.out.println("> Query:\t" + estimateQueryRequest);

            final String[] params = estimateQueryRequest.split("&");

            System.out.println("Workload");
            // Estimate request cost
            Request request = Server.getWorkloadEstimate(new Request(estimateQueryRequest, params));
            String query = originalQuery;

            if (request != null) {
                estimateWorkload = request.getNumberInstructions();

                // Check if we already know the exact cost of the request to avoid running instrumented code in the Worker Node
                if (request.getId().equals(originalQuery)) {
                    query += "&c=true";
                } else {
                    query += "&c=false";
                }

                System.out.println("Request received has " + request.getNumberInstructions() + " number of instructions");
            } else {
                query += "&c=false";
            }

            if (estimateWorkload + worker.getCurrentWorkload() > MAX_WORKLOAD) {
                System.out.println("Scaling up since laziest worker node will exceed the MAX_WORKLOAD supported.");
                //Server.requestScaleUp();
            }

            String queryUrlString = "http://" + workerIp + ":8000/scan?" + query;

            while (true) {
                try {
                    URL url = new URL(queryUrlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(REQUEST_TIMEOUT);
                    System.out.println("Load Balancer forwarding scan request to worker node with IP address " + workerIp);

                    worker.incrementCurrentNumberRequests();
                    worker.incrementCurrentWorkload(estimateWorkload);

                    int status = connection.getResponseCode();
                    if (status == HttpURLConnection.HTTP_OK) {
                        final Headers hdrs = t.getResponseHeaders();

                        hdrs.add("Content-Type", "image/png");

                        hdrs.add("Access-Control-Allow-Origin", "*");
                        hdrs.add("Access-Control-Allow-Credentials", "true");
                        hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
                        hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

                        t.sendResponseHeaders(200, 0);

                        final InputStream in = connection.getInputStream();
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        final OutputStream os = t.getResponseBody();

                        // Convert response body to byte array
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = in.read(bytes)) != -1) {
                            os.write(bytes, 0, length);
                        }

                        in.close();
                        reader.close();
                        os.close();

                        System.out.println("> Sent response to " + t.getRemoteAddress().toString());
                        worker.decrementCurrentNumberRequests();
                        worker.decrementCurrentWorkload(estimateWorkload);
                        break;
                    }
                    failedRequests++;
                    worker.decrementCurrentNumberRequests();
                    worker.decrementCurrentWorkload(estimateWorkload);
                } catch (Exception e) {
                    failedRequests++;
                    worker.decrementCurrentNumberRequests();
                    worker.decrementCurrentWorkload(estimateWorkload);
                }

                if (failedRequests >= MAX_REQUESTS) {
                    worker.setHealthy(false);
                    failedRequests = 0;

                    // Get a new healthy worker
                    worker = Server.getLaziestWorkerNode();
                    // If none found, request urgent scale up
                    if (worker == null) {
                        System.out.println("LoadBalancer found no available worker nodes requesting urgent scale up...");
                        //Server.requestScaleUp();
                        try {
                            System.out.println("LoadBalancer Starting to sleep...");
                            Thread.sleep(GRACE_PERIOD);
                            System.out.println("LoadBalancer fucking woke up...");
                        } catch (InterruptedException e) {
                            System.out.println("LoadBalancer caught exception");
                        }
                        while (worker == null) {
                            worker = Server.getLaziestWorkerNode();
                        }
                    }
                    workerIp = worker.getInstance().getPublicIpAddress();

                    queryUrlString = "http://" + workerIp + ":8000/scan?" + query;
                }
            }
        }
    }

    private void sendRequest(WorkerNode workerNode) {
        String workerIp = workerNode.getInstance().getPublicIpAddress();
        String queryUrlString = "http://" + workerIp + ":8000/healthcheck";
        int failedRequests = 0;

        while (true) {
            try {
                URL url = new URL(queryUrlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setConnectTimeout(HEALTH_CHECK_TIMEOUT);
                System.out.println("Load Balancer performing healthcheck to worker node with IP address " + workerIp);

                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    System.out.println("Worker node with IP address " + workerIp + " is healthy.");
                    break;
                }
                failedRequests++;
            } catch (Exception e) {
                failedRequests++;
            }

            if (failedRequests == HEALTH_CHECK_THESHOLD) {
                workerNode.setHealthy(false);
                System.out.println("Worker node with IP address " + workerIp + " is unhealthy.");
                break;
            }
        }
    }

    private void performHealthCheck() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable healthCheckTask = new Runnable() {
            public void run () {
                List<WorkerNode> workers = Server.getWorkers();
                for (WorkerNode workerNode : workers) {
                    final WorkerNode worker = workerNode;
                    Runnable sendRequestTask = new Runnable() {
                        public void run () {
                            sendRequest(worker);
                        }
                    };
                    new Thread(sendRequestTask).start();
                }
            }
        };
        executor.scheduleWithFixedDelay(healthCheckTask, 0, HEALTH_CHECK_PERIOD, TimeUnit.MILLISECONDS);
    }
}
