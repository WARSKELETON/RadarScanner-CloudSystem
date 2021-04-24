package pt.ulisboa.tecnico.cnv.server;


public class Request {

    enum Strategy {
        GRID_SCAN,
        PROGRESSIVE_SCAN,
        GREEDY_RANGE_SCAN
    }

    private final Strategy strategy;
    private final int width;
    private final int height;
    private final int viewportTopLeftX;
    private final int viewportTopLeftY;
    private final int viewportBottomRightX;
    private final int viewportBottomRightY;
    private final int startingPointX;
    private final int startingPointY;
    private final String imagePath;
    private final String outputDirPath;
    private Metrics metrics;

    public Request(Strategy strategy, int width, int height,
                         int viewportTopLeftX, int viewportTopLeftY,
                         int viewportBottomRightX, int viewportBottomRightY,
                         int startingPointX, int startingPointY,
                         String imagePath, String outputDirPath) {
        this.strategy = strategy;
        this.width = width;
        this.height = height;
        this.viewportTopLeftX = viewportTopLeftX;
        this.viewportTopLeftY = viewportTopLeftY;
        this.viewportBottomRightX = viewportBottomRightX;
        this.viewportBottomRightY = viewportBottomRightY;
        this.startingPointX = startingPointX;
        this.startingPointY = startingPointY;
        this.imagePath = imagePath;
        this.outputDirPath = outputDirPath;
        this.metrics = new Metrics();
    }

    //Getters
    public Strategy getStrategy() { return strategy; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getViewportTopLeftX() { return viewportTopLeftX; }
    public int getViewportTopLeftY() { return viewportTopLeftY; }
    public int getViewportBottomRightX() { return viewportBottomRightX; }
    public int getViewportBottomRightY() { return viewportBottomRightY; }
    public int getStartingPointX() { return startingPointX; }
    public int getStartingPointY() { return startingPointY; }
    public String getImagePath() { return imagePath; }
    public String getOutputDirPath() { return outputDirPath; }
}