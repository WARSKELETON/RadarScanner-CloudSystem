package pt.ulisboa.tecnico.cnv.server;

public class Metrics {
    private long numberInstructions;
    private boolean isComplete;

    public Metrics(){}

    public Metrics(long numberInstructions) {
        this.numberInstructions = numberInstructions;
        this.isComplete = false;
    }

    public long getNumberInstructions() {
        return numberInstructions;
    }

    public void setNumberInstructions(long numberInstructions) {
        this.numberInstructions = numberInstructions;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    @Override
    public String toString() {
        return "Metrics{" +
                "\nnumberInstructions=" + numberInstructions +
                "}\n";
    }
}