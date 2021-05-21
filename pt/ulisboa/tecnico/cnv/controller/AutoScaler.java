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

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class AutoScaler {

    public final long MAX_WORKLOAD = 50000000;
    public final double WORKLOAD_MAX_THRESHOLD = MAX_WORKLOAD * 0.70;
    public final double WORKLOAD_MIN_THRESHOLD = MAX_WORKLOAD * 0.30;
    public final double CPU_MAX_THRESHOLD = 0.7;
    public final double CPU_MIN_THRESHOLD = 0.30;

    public final int SCALING_STEP_UP = 1;
    public final int SCALING_STEP_DOWN = 1;

    public final int SCALE_PERIOD = 60000;
    public final int GRACE_PERIOD = 60000;
    public final int MIN_CAPACITY = 1;
    public final int MAX_CAPACITY = 2;

    private final String AMI_ID = "ami-0a8dbb932f9c08460";
    private final String KEY_NAME = "CNV-Lab-AWS";
    private final String SECURITY_GROUP = "CNV-SSH-HTTP";

    private String myInstanceId;

    private AmazonEC2 ec2;

    public AutoScaler() {
        try {
            initAWSClient();
            this.myInstanceId = EC2MetadataUtils.getInstanceId();
            initWorkerNodes();

            // TODO health check

            while (true) {
                Thread.sleep(SCALE_PERIOD);
                monitorWorkerNodes();
            }

        } catch (Exception e) {
            System.err.println("Caught exception");
        }
    }

    private void initAWSClient() throws Exception {
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
    }

    private void initWorkerNodes() throws Exception {
        try {
            // Get current workers
            DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
            List<Reservation> reservations = describeInstancesRequest.getReservations();

            for (Reservation reservation : reservations) {
                for (Instance instance : reservation.getInstances()) {
                    if (instance.getState().getName().equals("running")) {
                        if (instance.getInstanceId().equals(myInstanceId)) continue;

                        Server.addWorkerNode(instance);
                    }
                }
            }

            // Init system to the desired state
            int drift = Server.getNumberOfWorkerNodes() - MIN_CAPACITY;

            if (drift < 0) {
                createWorkerNodes(Math.abs(drift));
            } else if (drift > 0) {
                terminateWorkerNodes(drift);
            }
        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }
    }

    public void createWorkerNodes(int numberOfRequiredWorkers) {
        int futureNumberOfWorkers = Server.getNumberOfWorkers() + numberOfRequiredWorkers;

        if (futureNumberOfWorkers > MAX_CAPACITY) {
            numberOfRequiredWorkers = futureNumberOfWorkers - MAX_CAPACITY;
            if (numberOfRequiredWorkers == 0) return;

            System.out.println("Only creating " + numberOfRequiredWorkers + " worker nodes because MAX_CAPACITY was reached.");
        }

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(AMI_ID)
                .withInstanceType("t2.micro")
                .withMinCount(1)
                .withMaxCount(numberOfRequiredWorkers)
                .withKeyName(KEY_NAME)
                .withSecurityGroups(SECURITY_GROUP);

        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
        List<Instance> newWorkers = runInstancesResult.getReservation().getInstances();
        Set<String> workersIds = new HashSet<>();
        for (Instance instance : newWorkers) {
            workersIds.add(instance.getInstanceId());
        }

        System.out.println("Instance launching...");

        boolean done = false;
        while(!done) {

            try {
                Thread.sleep(GRACE_PERIOD);
            } catch (InterruptedException e) {
                System.err.println("Caught Exception");
            }

            // Request AWS for all instances states
            DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
            List<Reservation> reservations = describeInstancesRequest.getReservations();

            for (Reservation reservation : reservations) {
                for (Instance instance : reservation.getInstances()) {
                    // If it's not a new instance ignore
                    if (!workersIds.contains(instance.getInstanceId())) continue;

                    if (instance.getState().getName().equals("running")) {
                        done = true;
                        Server.addWorkerNode(instance);
                        System.out.println("New worker node with ID " + instance.getInstanceId() + " ready.");
                    } else {
                        done = false;
                        System.out.println("New worker node with ID " + instance.getInstanceId() + " has state " + instance.getState().getName() + ".");
                    }
                }
            }
        }
    }

    public void monitorWorkerNodes() {
        // TODO check if instances are healthy
        Server.updateCurrentCPUUsage();

        int totalCPUUtilization = 0;
        long totalCurrentWorkload = 0;
        int numberOfWorkers = Server.getWorkers().size();

        for (WorkerNode workerNode : Server.getWorkers()) {
            totalCPUUtilization += workerNode.getCurrentCPU();
            totalCurrentWorkload += workerNode.getCurrentWorkload();
        }

        double averageCPUUtilization = totalCPUUtilization / numberOfWorkers;
        double averageCurrentWorkload = totalCPUUtilization / numberOfWorkers;

        if (averageCurrentWorkload > WORKLOAD_MAX_THRESHOLD && numberOfWorkers < MAX_CAPACITY) {
            createWorkerNodes(SCALING_STEP_UP);
        } else if (averageCPUUtilization < CPU_MIN_THRESHOLD && averageCurrentWorkload < WORKLOAD_MIN_THRESHOLD && numberOfWorkers > MIN_CAPACITY) {
            terminateWorkerNodes(SCALING_STEP_DOWN);
        }
    }

    public void terminateWorkerNodes(int numberOfRequiredWorkers) {
        int futureNumberOfWorkers = Server.getNumberOfWorkers() - numberOfRequiredWorkers;

        if (futureNumberOfWorkers < MIN_CAPACITY) {
            numberOfRequiredWorkers = MIN_CAPACITY - futureNumberOfWorkers;
            if (numberOfRequiredWorkers == 0) return;

            System.out.println("Only terminating " + numberOfRequiredWorkers + " worker nodes because MAX_CAPACITY was reached.");
        }

        System.out.println("Instance termination...");
    }
}
