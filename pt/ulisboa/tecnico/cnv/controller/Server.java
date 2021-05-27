package pt.ulisboa.tecnico.cnv.controller;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.util.EC2MetadataUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
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

import pt.ulisboa.tecnico.cnv.mss.MSS;

public class Server {

    private static AmazonCloudWatch cloudWatch;
    private static LoadBalancer loadBalancer;
    private static AutoScaler autoScaler;
    private static MSS mss;

    private static boolean scaling = false;
    private static final Object workerLock = new Object();
    private static Map<String, WorkerNode> workers = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        initCloudWatch();
        mss = new MSS();
        mss.init();
        loadBalancer = new LoadBalancer();
        autoScaler = new AutoScaler();

        System.out.println("Controller initialized all modules.");

        while (true) {
        }
    }

    public synchronized static boolean isScaling() {
        return scaling;
    }

    public synchronized static void setScaling(boolean scaling) {
        Server.scaling = scaling;
    }

    public synchronized static List<WorkerNode> getWorkers() {
        List<WorkerNode> workerNodeList = new ArrayList<>();
        for (WorkerNode worker : workers.values()) {
            if (worker.getInstance().getState().getName().equals("running") && worker.isHealthy()) {
                workerNodeList.add(worker);
            }
        }
        return workerNodeList;
    }

    public synchronized static int getNumberOfWorkers() {
        return workers.size();
    }

    public static void initCloudWatch() {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        cloudWatch = AmazonCloudWatchClientBuilder.standard()
                .withRegion("us-east-1")
                .withCredentials(credentialsProvider)
                .build();
    }

    public static WorkerNode getLaziestWorkerNode() {
        WorkerNode laziestWorkerNode = null;

        synchronized (workerLock) {
            for (WorkerNode worker : workers.values()) {
                if (laziestWorkerNode == null && worker.getInstance().getState().getName().equals("running") && worker.isHealthy()) {
                    laziestWorkerNode = worker;
                } else if (laziestWorkerNode != null && worker.getCurrentWorkload() < laziestWorkerNode.getCurrentWorkload() && worker.getInstance().getState().getName().equals("running") && worker.isHealthy()) {
                    laziestWorkerNode = worker;
                }
            }
        }

        if (laziestWorkerNode != null) {
            System.out.println("Found laziest worker node with ID " + laziestWorkerNode.getInstance().getInstanceId());
        } else {
            System.out.println("Found no laziest worker node");
        }

        return laziestWorkerNode;
    }

    public static void addWorkerNode(Instance instance) {
        workers.put(instance.getInstanceId(), new WorkerNode(instance));
        System.out.println("Added worker node with ID " + instance.getInstanceId());
    }

    public static void removeWorkerNode(String instanceId) {
        workers.remove(instanceId);
        System.out.println("Removed worker node with ID " + instanceId);
    }

    public static void updateCurrentCPUUsage() {
        long offsetInMilliseconds = 1000 * 60 * 5; // 5 minutes

        Dimension dimension = new Dimension();
        dimension.setName("InstanceId");

        for (WorkerNode workerNode : workers.values()) {
            Instance instance = workerNode.getInstance();
            String instanceId = instance.getInstanceId();

            dimension.setValue(instanceId);

            GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
                    .withStartTime(new Date(new Date().getTime() - offsetInMilliseconds))
                    .withNamespace("AWS/EC2")
                    .withPeriod(60)
                    .withMetricName("CPUUtilization")
                    .withStatistics("Average")
                    .withDimensions(dimension)
                    .withEndTime(new Date());

            GetMetricStatisticsResult getMetricStatisticsResult = cloudWatch.getMetricStatistics(request);
            List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();

            Datapoint lastDatapoint = null;
            for (Datapoint dp : datapoints) {
                if (lastDatapoint == null) {
                    lastDatapoint = dp;
                }
                if (lastDatapoint.getTimestamp().before(dp.getTimestamp())) {
                    lastDatapoint = dp;
                }
            }

            double average;
            if (lastDatapoint != null) {
                average = lastDatapoint.getAverage();
            } else {
                average = 0;
            }

            workerNode.setCurrentCPU(average);
            System.out.println("Instance with ID " + instanceId + " with an average CPU utilization of " + average);
        }
    }

    public static void requestScaleUp() {
        Runnable task = new Runnable() {
            public void run () {
                autoScaler.createWorkerNodes(1);
                setScaling(false);
            }
        };

        if (!isScaling()) {
            setScaling(true);
            new Thread(task).start();
        }
    }

    public synchronized static int getNumberOfWorkerNodes() {
        return workers.values().size();
    }

    public static Request getWorkloadEstimate(Request request) {
        System.out.println("Estimating workload...");
        List<Request> requests = mss.getRequestById(request.getId());

        if (requests != null && !requests.isEmpty()) {
            System.out.println("Got exact match!");
            return requests.get(0);
        } else {
            requests = mss.getRequestsWithSimilarStrategyAndEqualMapSize(request.getStrategy(), request.getWidth(), request.getHeight(), request.getViewportArea());
            if (requests != null && !requests.isEmpty()) {
                System.out.println("Got match with similar strategy and equal map size!");
                List<Request> lowestViewportAreaDiff = mss.getRequestWithSimilarViewportArea(requests, request.getViewportArea());
                return mss.getWeightedAverageRequest(lowestViewportAreaDiff, request);
            } else {
                requests = mss.getRequestsWithEqualMapSize(request.getWidth(), request.getHeight(), request.getViewportArea());
                if (requests != null && !requests.isEmpty()) {
                    System.out.println("Got match with equal map size!");
                    List<Request> lowestViewportAreaDiff = mss.getRequestWithSimilarViewportArea(requests, request.getViewportArea());
                    return mss.getWeightedAverageRequest(lowestViewportAreaDiff, request);
                } else {
                    System.out.println("Got no match! Returning max workload");
                    return null;
                }
            }
        }
    }
}