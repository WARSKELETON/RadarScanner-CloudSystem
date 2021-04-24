package pt.ulisboa.tecnico.cnv.server;

public class Metrics {
    private long numberInstructions;
    private long basicBlocks;
    private long depthStack;
    private long callsFunctions;

    public Metrics(){}

    public Metrics(long numberInstructions, long basicBlocks, long depthStack, long callsFunctions) {
        this.numberInstructions = numberInstructions;
        this.basicBlocks = basicBlocks;
        this.depthStack = depthStack;
        this.callsFunctions = callsFunctions;
    }

    public long getNumberInstructions() { return numberInstructions; }
    public void setNumberInstructions(long numberInstructions) { this.numberInstructions = numberInstructions; }
    public long getBasicBlocks() { return basicBlocks; }
    public void setBasicBlocks(long basicBlocks) { this.basicBlocks = basicBlocks; }
    public long getDepthStack() { return depthStack; }
    public void setDepthStack(long depthStack) { this.depthStack = depthStack; }
    public long getCallsFunctions() { return callsFunctions; }
    public void setCallsFunctions(long callsFunctions) { this.callsFunctions = callsFunctions; }

    @Override
    public String toString() {
        return "Metrics{" +
                "numberInstructions=" + numberInstructions +
                ", basicBlocks=" + basicBlocks +
                ", depthStack=" + depthStack +
                ", callsFunctions=" + callsFunctions +
                '}';
    }
}