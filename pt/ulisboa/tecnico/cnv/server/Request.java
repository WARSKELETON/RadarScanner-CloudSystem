package pt.ulisboa.tecnico.cnv.server;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "scan-requests-table")
public class Request {

    private String id;
    private int width;
    private int height;
    private int viewportTopLeftX;
    private int viewportTopLeftY;
    private int viewportBottomRightX;
    private int viewportBottomRightY;
    private int startingPointX;
    private int startingPointY;
    private String strategy;
    private String image;
    private final Metrics metrics;

    public Request(String[] args){
        for (int i = 0; i < args.length; i += 2){
            switch (args[i]){
                case "-w":
                    this.width = Integer.parseInt(args[i+1]);
                    break;
                case "-h":
                    this.height = Integer.parseInt(args[i+1]);
                    break;
                case "-x0":
                    this.viewportTopLeftX = Integer.parseInt(args[i+1]);
                    break;
                case "-x1":
                    this.viewportBottomRightX = Integer.parseInt(args[i+1]);
                    break;
                case "-y0":
                    this.viewportTopLeftY = Integer.parseInt(args[i+1]);
                    break;
                case "-y1":
                    this.viewportBottomRightY = Integer.parseInt(args[i+1]);
                    break;
                case "-xS":
                    this.startingPointX = Integer.parseInt(args[i+1]);
                    break;
                case "-yS":
                    this.startingPointY = Integer.parseInt(args[i+1]);
                    break;
                case "-i":
                    this.image = args[i+1];
                    break;
                case "-s":
                    this.strategy = args[i+1];
                    break;
                default:
                    //should not reach here
            }
        }
        buildRequestId(args);
        this.metrics = new Metrics();
    }

    public void buildRequestId(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (String string : args) {
            builder.append(string);
        }
        this.setId(builder.toString());
        System.out.println("Request ID: " + id);
    }

    //Getters

    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    @DynamoDBAttribute(attributeName = "width")
    public int getWidth() { return width; }

    @DynamoDBAttribute(attributeName = "height")
    public int getHeight() { return height; }

    @DynamoDBAttribute(attributeName = "x0")
    public int getViewportTopLeftX() { return viewportTopLeftX; }

    @DynamoDBAttribute(attributeName = "y0")
    public int getViewportTopLeftY() { return viewportTopLeftY; }

    @DynamoDBAttribute(attributeName = "x1")
    public int getViewportBottomRightX() { return viewportBottomRightX; }

    @DynamoDBAttribute(attributeName = "y1")
    public int getViewportBottomRightY() { return viewportBottomRightY; }

    @DynamoDBAttribute(attributeName = "xS")
    public int getStartingPointX() { return startingPointX; }

    @DynamoDBAttribute(attributeName = "xY")
    public int getStartingPointY() { return startingPointY; }

    @DynamoDBAttribute(attributeName = "strategy")
    public String getStrategy() { return strategy; }

    @DynamoDBAttribute(attributeName = "image")
    public String getImage() { return image; }

    @DynamoDBIgnore
    public Metrics getMetrics() { return metrics; }

    public void setId(String id) {
        this.id = id;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setViewportTopLeftX(int viewportTopLeftX) {
        this.viewportTopLeftX = viewportTopLeftX;
    }

    public void setViewportTopLeftY(int viewportTopLeftY) {
        this.viewportTopLeftY = viewportTopLeftY;
    }

    public void setViewportBottomRightX(int viewportBottomRightX) {
        this.viewportBottomRightX = viewportBottomRightX;
    }

    public void setViewportBottomRightY(int viewportBottomRightY) {
        this.viewportBottomRightY = viewportBottomRightY;
    }

    public void setStartingPointX(int startingPointX) {
        this.startingPointX = startingPointX;
    }

    public void setStartingPointY(int startingPointY) {
        this.startingPointY = startingPointY;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "\nRequest{" +
                "\nwidth=" + width +
                ",\nheight=" + height +
                ",\nviewportTopLeftX=" + viewportTopLeftX +
                ",\nviewportTopLeftY=" + viewportTopLeftY +
                ",\nviewportBottomRightX=" + viewportBottomRightX +
                ",\nviewportBottomRightY=" + viewportBottomRightY +
                ",\nstartingPointX=" + startingPointX +
                ",\nstartingPointY=" + startingPointY +
                ",\nstrategy='" + strategy + '\'' +
                ",\nimage='" + image + '\'' +
                ",\nmetrics=" + metrics +
                "}\n";
    }
}