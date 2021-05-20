package ulisboa.tecnico.cnv.controller;

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

public class AutoScaler {

    public final int DESIRED_CAPACITY = 1;
    public final int MIN_CAPACITY = 1;
    public final int MAX_CAPACITY = 2;

    private final String AMI_ID = "ami-0a8dbb932f9c08460";
    private final String KEY_NAME = "CNV-Lab-AWS";
    private final String SECURITY_GROUP = "CNV-SSH-HTTP";

    private String myInstanceId;

    private AmazonEC2 ec2;
    private AmazonCloudWatch cloudWatch;

    public AutoScaler() {
        initAWSClient();
        this.instanceId = EC2MetadataUtils.getInstanceId();
        initWorkerNodes();
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
        cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
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
            int drift = Server.getNumberOfWorkerNodes() - DESIRED_CAPACITY;

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
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(AMI_ID)
                .withInstanceType("t2.micro")
                .withMinCount(1)
                .withMaxCount(numberOfRequiredWorkers)
                .withKeyName(KEY_NAME)
                .withSecurityGroups(SECURITY_GROUP);

        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);

        System.out.println("Instance launching...");
    }

    public void terminateWorkerNodes(int numberOfRequiredWorkers) {
        System.out.println("Instance termination...");
    }
}
