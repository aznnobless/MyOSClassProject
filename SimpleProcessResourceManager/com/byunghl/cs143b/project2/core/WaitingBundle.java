package com.byunghl.cs143b.project2.core;

public class WaitingBundle {

    private PCB ownerOfBundle;
    private int numberOfDemand;
    private String resourceId;

    public WaitingBundle(PCB pcb, String id, int demand) {
        this.ownerOfBundle = pcb;
        this.resourceId = id;
        this.numberOfDemand = demand;
    }

    public PCB getOwnerOfBundle() {
        return ownerOfBundle;
    }

    public String getResourceId() {
        return resourceId;
    }

    public int getNumberOfDemand() {
        return numberOfDemand;
    }
}
