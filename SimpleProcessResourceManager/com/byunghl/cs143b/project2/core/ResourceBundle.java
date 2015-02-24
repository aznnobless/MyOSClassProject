package com.byunghl.cs143b.project2.core;

/**
 * This Bundle contain resource unit and will be carried by process.
 */
public class ResourceBundle {

    private int numberOfUnit;
    private String resourceId;

    public ResourceBundle(String resourceId, int numberOfUnit ) {
        this.resourceId = resourceId;
        this.numberOfUnit = numberOfUnit;
    }

    public int getNumberOfUnit() {
        return numberOfUnit;

    }

    public String getResourceId() {
        return resourceId;
    }

    public void setNumberOfUnit(int amt) {
        numberOfUnit = amt;
    }

    @Override
    public String toString() {
        return "[resourceId:"+ resourceId +"][numberOfUnit:"+numberOfUnit+"]";
    }
}
