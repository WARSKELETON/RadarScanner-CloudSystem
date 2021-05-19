package pt.ulisboa.tecnico.cnv.controller;

import com.amazonaws.services.ec2.model.Instance;

public class WorkerNode {

    private Instance instance;
    private String id;
    private int currentNumberRequests;
    private long currentWorkload;
    private int currentCPU;

    public WorkerNode (Instance instance) {
        this.instance = instance;
        this.id = instance.getInstanceId();
        this.currentNumberRequests = 0;
        this.currentWorkload = 0;
        this.currentCPU = 0;
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public int getCurrentNumberRequests () {
        return currentNumberRequests;
    }

    public void setCurrentNumberRequests (int currentNumberRequests) {
        this.currentNumberRequests = currentNumberRequests;
    }

    public long getCurrentWorkload () {
        return currentWorkload;
    }

    public void setCurrentWorkload (long currentWorkload) {
        this.currentWorkload = currentWorkload;
    }

    public int getCurrentCPU () {
        return currentCPU;
    }

    public void setCurrentCPU (int currentCPU) {
        this.currentCPU = currentCPU;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}