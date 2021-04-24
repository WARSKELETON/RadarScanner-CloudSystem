package pt.ulisboa.tecnico.cnv.server;


public class Request {

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
        this.metrics = new Metrics();
    }

    //Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getViewportTopLeftX() { return viewportTopLeftX; }
    public int getViewportTopLeftY() { return viewportTopLeftY; }
    public int getViewportBottomRightX() { return viewportBottomRightX; }
    public int getViewportBottomRightY() { return viewportBottomRightY; }
    public int getStartingPointX() { return startingPointX; }
    public int getStartingPointY() { return startingPointY; }
    public String getStrategy() { return strategy; }
    public String getImage() { return image; }
    public Metrics getMetrics() { return metrics; }

    @Override
    public String toString() {
        return "Request{" +
                "width=" + width +
                ", height=" + height +
                ", viewportTopLeftX=" + viewportTopLeftX +
                ", viewportTopLeftY=" + viewportTopLeftY +
                ", viewportBottomRightX=" + viewportBottomRightX +
                ", viewportBottomRightY=" + viewportBottomRightY +
                ", startingPointX=" + startingPointX +
                ", startingPointY=" + startingPointY +
                ", strategy='" + strategy + '\'' +
                ", image='" + image + '\'' +
                ", metrics=" + metrics +
                '}';
    }
}