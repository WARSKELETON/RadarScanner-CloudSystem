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
    private long viewportArea;
    private int startingPointX;
    private int startingPointY;
    private String strategy;
    private String image;
    private long numberInstructions;
    private boolean isComplete;

    public Request(){
    }

    public Request(String query, String[] args){
        for (int i = 0; i < args.length; i++){
            final String[] params = args[i].split("=");
            switch (params[0]) {
                case "w":
                    this.width = Integer.parseInt(params[1]);
                    break;
                case "h":
                    this.height = Integer.parseInt(params[1]);
                    break;
                case "x0":
                    this.viewportTopLeftX = Integer.parseInt(params[1]);
                    break;
                case "x1":
                    this.viewportBottomRightX = Integer.parseInt(params[1]);
                    break;
                case "y0":
                    this.viewportTopLeftY = Integer.parseInt(params[1]);
                    break;
                case "y1":
                    this.viewportBottomRightY = Integer.parseInt(params[1]);
                    break;
                case "xS":
                    this.startingPointX = Integer.parseInt(params[1]);
                    break;
                case "yS":
                    this.startingPointY = Integer.parseInt(params[1]);
                    break;
                case "i":
                    this.image = params[1];
                    break;
                case "s":
                    this.strategy = params[1];
                    break;
                case "c":
                    this.isComplete = Boolean.parseBoolean(params[1]);
                    break;
                default:
                    //should not reach here
            }
        }
        this.id = query.split("&c=")[0];
        this.numberInstructions = 0;
        this.viewportArea = (viewportBottomRightX - viewportTopLeftX) * (viewportBottomRightY - viewportTopLeftY);
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

    @DynamoDBAttribute(attributeName = "viewportArea")
    public long getViewportArea () {
        return viewportArea;
    }

    @DynamoDBAttribute(attributeName = "xS")
    public int getStartingPointX() { return startingPointX; }

    @DynamoDBAttribute(attributeName = "yS")
    public int getStartingPointY() { return startingPointY; }

    @DynamoDBAttribute(attributeName = "strategy")
    public String getStrategy() { return strategy; }

    @DynamoDBAttribute(attributeName = "image")
    public String getImage() { return image; }

    @DynamoDBAttribute(attributeName = "numberInstructions")
    public long getNumberInstructions() {
        return numberInstructions;
    }

    @DynamoDBIgnore
    public boolean isComplete() {
        return isComplete;
    }

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

    public void setViewportArea (long viewportArea) {
        this.viewportArea = viewportArea;
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

    public void setNumberInstructions(long numberInstructions) {
        this.numberInstructions = numberInstructions;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    @Override
    public String toString() {
        return "Request{" +
                "id='" + id + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", viewportTopLeftX=" + viewportTopLeftX +
                ", viewportTopLeftY=" + viewportTopLeftY +
                ", viewportBottomRightX=" + viewportBottomRightX +
                ", viewportBottomRightY=" + viewportBottomRightY +
                ", viewportArea=" + viewportArea +
                ", startingPointX=" + startingPointX +
                ", startingPointY=" + startingPointY +
                ", strategy='" + strategy + '\'' +
                ", image='" + image + '\'' +
                ", numberInstructions=" + numberInstructions +
                ", isComplete=" + isComplete +
                '}';
    }
}