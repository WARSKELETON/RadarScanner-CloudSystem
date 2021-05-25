package pt.ulisboa.tecnico.cnv.mss;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import pt.ulisboa.tecnico.cnv.server.Request;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

public class MSS {
    private static final long VIEWPORT_AREA_64 = 4096;
    private static final long VIEWPORT_AREA_128 = 16384;
    private static final long VIEWPORT_AREA_256 = 65536;
    private static final long VIEWPORT_AREA_512 = 262144;
    private static final long VIEWPORT_AREA_1024 = 1048576;
    private static final long VIEWPORT_AREA_2048 = 4194304;

    static AmazonDynamoDB dynamoDB;
    public static String tableName = "scan-requests-table";
    private static DynamoDBMapper mapper;

    public static void init() throws Exception {
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
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-1")
                .build();

        mapper = new DynamoDBMapper(dynamoDB);
    }

    public static void main(String[] args) throws Exception {
        init();

        try {
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                    .withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

            TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
            TableUtils.waitUntilActive(dynamoDB, tableName);

            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    public static List<Request> getRequestById(String query) {
        Request partitionKey = new Request();
        partitionKey.setId(query);
        DynamoDBQueryExpression<Request> queryExpression = new DynamoDBQueryExpression<Request>()
            .withHashKeyValues(partitionKey);

        return mapper.query(Request.class, queryExpression);
    }

    public static List<Request> getRequestsWithSimilarStrategyAndEqualMapSize(String strategy, int width, int height, long viewportArea) {
        List<Long> viewportAreaBounds = getUpperAndLowerViewportAreaBound(viewportArea);

        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(":width", new AttributeValue().withN(String.valueOf(width)));
        attributeValues.put(":height", new AttributeValue().withN(String.valueOf(height)));
        attributeValues.put(":viewportAreaLowerBound", new AttributeValue().withN(String.valueOf(viewportAreaBounds.get(0))));
        attributeValues.put(":viewportAreaUpperBound", new AttributeValue().withN(String.valueOf(viewportAreaBounds.get(1))));
        attributeValues.put(":mainStrategy", new AttributeValue().withS(strategy));

        if (strategy.equals("GRID_SCAN")) {
            attributeValues.put(":secondaryStrategy", new AttributeValue().withS(strategy));
        } else {
            if (strategy.equals("PROGRESSIVE_SCAN")) attributeValues.put(":secondaryStrategy", new AttributeValue().withS("GREEDY_RANGE_SCAN"));
            if (strategy.equals("GREEDY_RANGE_SCAN")) attributeValues.put(":secondaryStrategy", new AttributeValue().withS("PROGRESSIVE_SCAN"));
        }

        Request partitionKey = new Request();

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("(strategy = :mainStrategy or strategy = :secondaryStrategy) and width = :width and height = :height and viewportArea between :viewportAreaLowerBound and :viewportAreaUpperBound")
                .withExpressionAttributeValues(attributeValues);

        return mapper.parallelScan(Request.class, scanExpression, 16);
    }

    public static List<Request> getRequestsWithEqualMapSize(int width, int height, long viewportArea) {
        List<Long> viewportAreaBounds = getUpperAndLowerViewportAreaBound(viewportArea);

        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(":width", new AttributeValue().withN(String.valueOf(width)));
        attributeValues.put(":height", new AttributeValue().withN(String.valueOf(height)));
        attributeValues.put(":viewportAreaLowerBound", new AttributeValue().withN(String.valueOf(viewportAreaBounds.get(0))));
        attributeValues.put(":viewportAreaUpperBound", new AttributeValue().withN(String.valueOf(viewportAreaBounds.get(1))));

        Request partitionKey = new Request();

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("width = :width and height = :height and viewportArea between :viewportAreaLowerBound and :viewportAreaUpperBound")
                .withExpressionAttributeValues(attributeValues);

        return mapper.parallelScan(Request.class, scanExpression, 16);
    }

    public static Request getWeightedAverageRequest(List<Request> requests, Request originalRequest) {
        System.out.println("MSS is calculating weighted average...");
        double distancesSum = 0;
        double sum = 0;
        Request mostSimilarRequest = new Request();
        mostSimilarRequest.setId("noid");
        double originalDistance = getStartingPointDistanceToUpperLeftCorner(originalRequest);

        for (Request request : requests) {
            double distance = getStartingPointDistanceToUpperLeftCorner(request);
            double requestWeight = Math.abs(originalDistance - distance);
            distancesSum += requestWeight;
            sum += requestWeight * request.getNumberInstructions();
            System.out.println("Request with viewport area of " + request.getViewportArea() + " with distance " + distance + " got weight " + requestWeight + " . Sum is at " + sum + " and weights at " + distancesSum);
        }

        mostSimilarRequest.setNumberInstructions((long)Math.ceil(sum / distancesSum));

        System.out.println("Final result of number of instructions is " + mostSimilarRequest.getNumberInstructions());

        return mostSimilarRequest;
    }

    public static List<Request> getRequestWithSimilarViewportArea(List<Request> requests, long viewportArea) {
        long smallestDifference = Long.MAX_VALUE;
        List<Request> similarRequests = new ArrayList<>();
        Request mostSimilarRequest = null;

        for (Request request : requests) {
            long difference = Math.abs(request.getViewportArea() - viewportArea);
            if (difference < smallestDifference) {
                smallestDifference = difference;
                mostSimilarRequest = request;
            }
        }

        if (mostSimilarRequest != null) {
            for (Request request : requests) {
                if (mostSimilarRequest.getViewportArea() == request.getViewportArea()) {
                    similarRequests.add(request);
                }
            }
        }

        return similarRequests;
    }

    private static double getStartingPointDistanceToUpperLeftCorner(Request request) {
        int requestXS = request.getStartingPointX() - request.getViewportTopLeftX();
        int requestYS = request.getStartingPointY() - request.getViewportTopLeftY();

        return Math.sqrt(Math.pow(requestYS, 2) + Math.pow(requestYS, 2));
    }

    private static List<Long> getUpperAndLowerViewportAreaBound(long viewportArea) {
        List<Long> bounds = new ArrayList<>();

        if (viewportArea <= VIEWPORT_AREA_64) {
            bounds.add((long) 0);
            bounds.add(VIEWPORT_AREA_64);
        } else if (viewportArea > VIEWPORT_AREA_64 && viewportArea <= VIEWPORT_AREA_128) {
            bounds.add(VIEWPORT_AREA_64);
            bounds.add(VIEWPORT_AREA_128);
        } else if (viewportArea > VIEWPORT_AREA_128 && viewportArea <= VIEWPORT_AREA_256) {
            bounds.add(VIEWPORT_AREA_128);
            bounds.add(VIEWPORT_AREA_256);
        } else if (viewportArea > VIEWPORT_AREA_256 && viewportArea <= VIEWPORT_AREA_512) {
            bounds.add(VIEWPORT_AREA_256);
            bounds.add(VIEWPORT_AREA_512);
        } else if (viewportArea > VIEWPORT_AREA_512 && viewportArea <= VIEWPORT_AREA_1024) {
            bounds.add(VIEWPORT_AREA_512);
            bounds.add(VIEWPORT_AREA_1024);
        } else if (viewportArea > VIEWPORT_AREA_1024 && viewportArea <= VIEWPORT_AREA_2048) {
            bounds.add(VIEWPORT_AREA_1024);
            bounds.add(VIEWPORT_AREA_2048);
        }
        return bounds;
    }

    public static void saveRequest(Request request) {
        mapper.save(request);
    }
}