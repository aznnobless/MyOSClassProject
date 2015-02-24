package com.byunghl.cs143b.project2.core;

import com.byunghl.cs143b.project2.interfaces.PrivateList;

/**
 * Representation of Resource
 *
 *      All resources are static and their RCBs are created by the system at start-up time.
 */

public class RCB {

    private String resourceID ; // RID is a unique identifier by which the resource may be referred to by proceses. 1 ~ 4
    private Status status;
    private PrivateList<WaitingBundle> waitingList; // List of blocked process

    // Default Constructor
    private RCB() {
        // intentionally empty
    }

    // Constructor
    public RCB(String id, int quantity) {
        this.resourceID = id;
        this.status = new Status(quantity);
        this.waitingList = new WaitingList();

    }

    public void request(int number) {
        for(int i = 0 ; i < number; i++) {
            status.allocate();
        }
    }

    public void release(int number) {
        for(int i = 0; i < number; i++) {
            status.deallocate();
        }
    }

    public String getResourceID() {
        return resourceID;
    }

    public int getTotalNumberOfUnit() {
        return status.getNumberOfAllocatedUnit() + status.getNumberOfFreeUnit();
    }

    // Getter:  Return number of free unit
    public int getNumberOfFreeUnit() {
        return status.getNumberOfFreeUnit();
    }

    // Getter: Return number of allocated unit
    public int getNumberOfAllocatedUnit() {
        return status.getNumberOfAllocatedUnit();
    }

    // Getter: Return a waiting list
    public PrivateList getWatingList() {
        return waitingList;
    }

    /* * *
     * Inner class : Status
     * Status indicates whether the resource is currently free or allocated to other process.
     */

    private class Status {

        private int numberOfFreeUnit;
        private int numberOfAllocatedUnit;

        // Constructor
        public Status(int numberOfUnits) {
            this.numberOfFreeUnit = numberOfUnits;
            this.numberOfAllocatedUnit = 0;
        }

        // allocate a resource unit
        public void allocate(){

            if(numberOfFreeUnit == 0) {
                System.err.println("ERROR@RCB.Status.allocate() : No free unit");
                return;
            }

            numberOfFreeUnit -= 1;
            numberOfAllocatedUnit += 1;

        }

        // deallocate a resource unit
        public void deallocate() {

            if(numberOfAllocatedUnit == 0){
                System.err.println("ERROR@RCB.Status.deallocate() : No allocated unit");
                return;
            }
            numberOfAllocatedUnit -= 1;
            numberOfFreeUnit += 1;

        }

        // Getter
        public int getNumberOfFreeUnit() {
            return numberOfFreeUnit;
        }

        // Getter
        public int getNumberOfAllocatedUnit() {
            return numberOfAllocatedUnit;
        }
    }
    

}
