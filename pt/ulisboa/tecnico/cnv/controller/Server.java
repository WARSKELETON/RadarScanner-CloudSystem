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

    private static LoadBalancer loadBalancer;
    private static AutoScaler autoScaler;
    private static MSS mss;

    private static final Object workerLock = new Object();
    private static Map<String, WorkerNode> workers = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        loadBalancer = new LoadBalancer();
        autoScaler = new AutoScaler();
        mss = new MSS();
        mss.init();

        while(true) {
        }
    }

    public static WorkerNode getLaziestWorkerNode() {
        WorkerNode laziestWorkerNode = null;

        synchronized (workerLock) {
            for (WorkerNode worker : workers.values()) {
                if (laziestWorkerNode == null || worker.getCurrentWorkload() < laziestWorkerNode.getCurrentWorkload()) {
                    laziestWorkerNode = worker;
                }
            }
        }

        System.out.println("Found laziest worker node with ID " + laziestWorkerNode.getInstance().getInstanceId());

        return laziestWorkerNode;
    }

    public static void addWorkerNode(Instance instance) {
        workers.put(instance.getInstanceId(), new WorkerNode(instance));
        System.out.println("Added worker node with ID " + instance.getInstanceId());
    }

    public static int getNumberOfWorkerNodes() {
        return workers.values().size();
    }

    public static Request getWorkloadEstimate(Request request) {
        List<Request> requests = mss.getRequestById(request.getId());

        if (!requests.isEmpty()) {
            return requests.get(0);
        } else {
            requests = mss.getSimilarRequests(request.getStrategy(), request.getWidth(), request.getHeight(), request.getViewportArea());
            if (!requests.isEmpty()) {
                return requests.get(0);
            } else {
                return null;
            }
        }
    }
}