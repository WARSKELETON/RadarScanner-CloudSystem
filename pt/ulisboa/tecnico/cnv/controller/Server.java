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

    private static String AMI_ID = "ami-0a8dbb932f9c08460";
    private static String KEY_NAME = "CNV-Lab-AWS";
    private static String SECURITY_GROUP = "CNV-SSH-HTTP";

    private static AmazonEC2 ec2;
    private static AmazonCloudWatch cloudWatch;

    private static LoadBalancer loadBalancer;
    //private AutoScaler autoScaler;
    private static MSS mss;

    private static final Object workerLock = new Object();
    private static Map<String, WorkerNode> workers = new ConcurrentHashMap<>();

    private static void initAWSClient() throws Exception {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    public static void main(String[] args) throws Exception {
        initAWSClient();
        //createInstances(2);

        loadBalancer = new LoadBalancer();
        //this.autoScaler = new AutoScaler();
        mss = new MSS();

        while(true) {
        }
    }

    public static void createInstances(int numberOfInstances) {
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(AMI_ID)
                .withInstanceType("t2.micro")
                .withMinCount(1)
                .withMaxCount(numberOfInstances)
                .withKeyName(KEY_NAME)
                .withSecurityGroups(SECURITY_GROUP);

        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);

        System.out.println("Instance launching...");
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

        return laziestWorkerNode;
    }
}