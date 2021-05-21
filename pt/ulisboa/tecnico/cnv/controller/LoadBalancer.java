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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.Executors;

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

    public static final long MAX_WORKLOAD = 50000000;
    public static final int REQUEST_TIMEOUT = 300000;
    public static final int MAX_REQUESTS = 3;
    private static final String LOCAL_IP = "0.0.0.0";

    // TODO heath check

    public LoadBalancer() {
        HttpServer server = null;
        try {    
            //server = HttpServer.create(new InetSocketAddress(80), 0);
            server = HttpServer.create(new InetSocketAddress(LOCAL_IP, 8000), 0);
        } catch (Exception e) {
            System.err.println("Failed to launch load balancer " + e.getMessage());
        }

        if (server == null) return;

        server.createContext("/scan", new MyScanHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println(server.getAddress().toString());
    }

    static class MyScanHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange t) throws IOException {
            int failedRequests = 0;
            long estimateWorkload = MAX_WORKLOAD;

            final String query = t.getRequestURI().getQuery();
            WorkerNode worker = Server.getLaziestWorkerNode();
            String workerIp = worker.getInstance().getPublicIpAddress();
            //String workerIp = LOCAL_IP;

            System.out.println("> Query:\t" + query);

            final String[] params = query.split("&");

            // Estimate request cost
            Request request = Server.getWorkloadEstimate(new Request(query, params));

            if (request != null) {
                estimateWorkload = request.getNumberInstructions();
                System.out.println("Request received has " + request.getNumberInstructions() + " number of instructions");
            }

            if (estimateWorkload + worker.getCurrentWorkload() > MAX_WORKLOAD) {
                System.out.println("Scaling up since laziest worker node will exceed the MAX_WORKLOAD supported.");
                Server.requestScaleUp();
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

                        final InputStream in  = connection.getInputStream();
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
            }
        }
    }
}
