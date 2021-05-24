package pt.ulisboa.tecnico.cnv.controller;

import com.amazonaws.services.ec2.model.Instance;

public class WorkerNode {

    private Instance instance;
    private String id;
    private int currentNumberRequests;
    private long currentWorkload;
    private double currentCPU;
    private boolean healthy;

    public WorkerNode (Instance instance) {
        this.instance = instance;
        this.id = instance.getInstanceId();
        this.currentNumberRequests = 0;
        this.currentWorkload = 0;
        this.currentCPU = 0;
        this.healthy = true;
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

    public void incrementCurrentNumberRequests () {
        this.currentNumberRequests++;
    }

    public void decrementCurrentNumberRequests () {
        this.currentNumberRequests--;
    }

    public long getCurrentWorkload () {
        return currentWorkload;
    }

    public void setCurrentWorkload (long currentWorkload) {
        this.currentWorkload = currentWorkload;
    }

    public void incrementCurrentWorkload (long workload) {
        this.currentWorkload += workload;
    }

    public void decrementCurrentWorkload (long workload) {
        long finalWorkload = currentWorkload - workload;
        if (finalWorkload <= 0) {
            this.currentWorkload = 0;
        } else {
            this.currentWorkload = finalWorkload;
        }
    }

    public double getCurrentCPU () {
        return currentCPU;
    }

    public void setCurrentCPU (double currentCPU) {
        this.currentCPU = currentCPU;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
}