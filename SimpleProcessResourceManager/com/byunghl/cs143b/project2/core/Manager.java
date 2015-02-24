package com.byunghl.cs143b.project2.core;

import com.byunghl.cs143b.project2.interfaces.PrivateList;
import com.byunghl.cs143b.project2.interfaces.UserInterface;
import com.byunghl.cs143b.project2.state.BlockedState;
import com.byunghl.cs143b.project2.state.ReadyState;
import com.byunghl.cs143b.project2.state.RunningState;
import com.byunghl.cs143b.project2.utility.SystemUtility;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Manager implements UserInterface
{
    private boolean isAutomatedMode;

    private ReadyList readyList;
    private PCB rootPCB;
    private PCB currentRunningProcess;
    private List<RCB> availableResources;

    private BufferedWriter bufferedWriter;


    // Constructor
    public Manager(boolean mode) {
        init(mode); //initialize and create root
    }

    public Manager(boolean mode, BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
        init(mode);
    }

    @Override
    public void init(boolean mode) {

        setMode(mode); // set running mode : shell mode or automated mode

        initilizeResource(); // initialize resources

        currentRunningProcess = null; // set current running process null

        readyList = new ReadyList(); // Create a ready list

        rootPCB = new PCB("init", 0); // create init process
        rootPCB.changeState(ReadyState.getInstance()); // set init process state to ready
        readyList.add(rootPCB);// put init process to ready list

        preempt(rootPCB, false); // start init process
    }

    @Override
    public void quit() {
        System.exit(1);
    }

    @Override
    public void createProcess(String processId, int priority) {

        // Parameter error checking
        if(priority <= 0 || priority > 2) {

            String errorMessage = "error(@Manager.createProcess() : Invalid priority value "+  priority +" )" ;
            SystemUtility.getInstance().displayMessage(isAutomatedMode, errorMessage);

            return;
        }

        // Step 1. Check processName is exist or not.
        if(isProcessExist(rootPCB, processId)) {

            String errorMessage = "error(duplicate process name: " + processId +")";

            SystemUtility.getInstance().displayMessage(isAutomatedMode, errorMessage);

            return;
        }

        // Step 2. Create a process
        PCB process = new PCB(processId, priority);

        // Step 3. Link to current running process
        currentRunningProcess.addChildToTree(process);
        process.setParentToTree(currentRunningProcess);

        // Step 4. insert Process to ReadyList
        process.changeState(ReadyState.getInstance());
        readyList.add(process);
        process.setStatusList(readyList);

        // Step 5. reschedule
        scheduler();

    }

    @Override
    public void destroyProcess(String processId) {

        if(processId.equals("init")) {
            SystemUtility.getInstance().displayMessage(isAutomatedMode, "error(init process cannot be destroyed.)");
            return;
        }
        PCB temp = retrievePCBFromTree(rootPCB, processId);

        // Step1 : Check process exist or not.
        if(temp == null) {

            SystemUtility.getInstance().displayMessage(isAutomatedMode,
                        "Error@Manager.destroyProcess() : target process does not exist");

            return;

        }

        // Step2 : kill tree
        killTree(temp);

        // Step 3: reschedule
        scheduler();

    }

    /*
     * Need test
     */
    private void killTree(PCB targetPCB){

        List<PCB> children = targetPCB.getChildrenFromTree();

        int numberOfChildren = children.size();

        /**
         *  Coding Tip: if you need to delete an element from collection while you are iterating,
         *  don't use foreach or iterator.
         *  It does not work as what you expected.
         *  Hard to find a bug if this happens.
         */

        for(int i = 0; i < numberOfChildren; i++) {

            PCB current = children.get(0);

            // if current has children
            if(!current.isLeafProcess()) {

                killTree(current); //recursively traverse
            }

            PrivateList tempPriveList = current.getStatusList();

            if( (tempPriveList instanceof WaitingList) && current.getStatusList().size() > 0) {

                if(current.getStatusList() instanceof ReadyList) {
                    // do nothing.
                } else {

                    WaitingList wl = (WaitingList)current.getStatusList();

                    int numberOfBundleInWaitingList = current.getStatusList().size();

                    for(int index = 0 ; index < numberOfBundleInWaitingList; index++) {
                        WaitingBundle bundle = wl.get(0);
                        if(bundle.getOwnerOfBundle().getId().equals(current.getId())) {
                            wl.remove(bundle);
                        }
                    }
                }

            }

            //Free Resource
            if(current.getOtherResources().size() > 0) {

                int numberOfBunldleInOtherResources = current.getOtherResources().size();

                for(int index = 0 ; index < numberOfBunldleInOtherResources; index++) {
                    ResourceBundle bundle = current.getOtherResources().get(0);

                    releaseResource(bundle.getResourceId(), bundle.getNumberOfUnit(), current, true);
                }

            }

            if(currentRunningProcess!=null && currentRunningProcess.equals(current)) {

                currentRunningProcess = null;

            } else {
                readyList.remove(current);
            }

            current.getParentFromTree().getChildrenFromTree().remove(current);


        } // End of for (Chilren traverse logic end)

        //System.out.println("is ReadyList? : " + (targetPCB.getStatusList() instanceof ReadyList));
        //System.out.println("is WaitingList? : " + (targetPCB.getStatusList() instanceof WaitingList));

        PrivateList tempPriveList = targetPCB.getStatusList();

        // Remove waitlist from targetPCB
        if( (tempPriveList instanceof WaitingList) && targetPCB.getStatusList().size() > 0) {

            if(targetPCB.getStatusList() instanceof ReadyList) {
                // do nothing.
            } else {

                WaitingList wl = (WaitingList)targetPCB.getStatusList();

                int numberOfBundleInWaitingList = targetPCB.getStatusList().size();

                for (int index = 0; index < numberOfBundleInWaitingList; index++) {
                    WaitingBundle bundle = wl.get(0);
                    if (bundle.getOwnerOfBundle().getId().equals(targetPCB.getId())) {
                        wl.remove(bundle);
                    }
                }

            }


        }

        //Free Resource from targetPCB
        if(targetPCB.getOtherResources().size() > 0) {
            int numberOfBunldleInOtherResources = targetPCB.getOtherResources().size();
            for(int index = 0 ; index < numberOfBunldleInOtherResources; index++) {
                ResourceBundle bundle = targetPCB.getOtherResources().get(0);

                releaseResource(bundle.getResourceId(), bundle.getNumberOfUnit(), targetPCB, true);
            }
        }

        if(targetPCB.getParentFromTree().equals(rootPCB)) {
            rootPCB.getChildrenFromTree().clear();
        }else {
            targetPCB.getParentFromTree().getChildrenFromTree().remove(targetPCB);
        }

        if(currentRunningProcess!= null && currentRunningProcess.equals(targetPCB)) {
            currentRunningProcess = null;
        }else {

            readyList.remove(targetPCB);

        }

    }

    @Override
    public void requestResource(String resourceId, int numberOfUnit) {

        // Check current process is init process; init process cannot request resource
        if(currentRunningProcess.getsPriority() == 0) {

            SystemUtility.getInstance().displayMessage(isAutomatedMode,
                        "ERROR@Manger.requestResource() : this process cannot request resource");

            return;
        }

        // check resource id is valid or not ; valid id : R1 ~ R4
        if(!isValidResourceName(resourceId)) {

            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("error(non-existent resource: ")
                    .append(parseResourceNumberFromResourceId(resourceId)).append(")");
            SystemUtility.getInstance().displayMessage(isAutomatedMode, errorMessage.toString());

            return;
        }

        // Retrieve Resource
        RCB targetRCB = retrieveRCBFromAvailableResources(resourceId);
        WaitingList wl = (WaitingList)targetRCB.getWatingList();
        try {
            // ERROR CHECK : request amount cannot exceed maximum size of the resource
            if(numberOfUnit > targetRCB.getTotalNumberOfUnit() ) {

                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("error(request too many units: ").append(numberOfUnit).append("/")
                            .append(resourceId).append(")");
                SystemUtility.getInstance().displayMessage( isAutomatedMode, errorMessage.toString() );

                return;
            }

            /* CASE 1: resource has enough available resource units */
            if(targetRCB.getNumberOfFreeUnit() >= numberOfUnit ) {

                // Check PCB posses a resource or not
                if(currentRunningProcess.getOtherResources().size() > 0) {
                    boolean isFound = false;
                    for(ResourceBundle resourceBundle : currentRunningProcess.getOtherResources()) {
                        if(resourceBundle.getResourceId().equals(resourceId)) {
                            isFound = true;
                            resourceBundle.setNumberOfUnit(resourceBundle.getNumberOfUnit() + numberOfUnit);
                        }
                    }
                    if(!isFound)
                        currentRunningProcess.getOtherResources().add( new ResourceBundle(resourceId, numberOfUnit) );
                } else {
                    // allocate resource to currently running process
                    currentRunningProcess.getOtherResources().add( new ResourceBundle(resourceId, numberOfUnit) );
                }


                // RCB unit computation
                targetRCB.request(numberOfUnit);
                try {
                    displayReplyMessage(currentRunningProcess);
                }catch(IOException ex) {
                    ex.printStackTrace();
                    System.err.println("Point : MAGIC");
                }
            }
            /* CASE 2: resource has not enough resource units, */
            else {
                currentRunningProcess.changeState(BlockedState.getInstance());
                wl.add(new WaitingBundle(currentRunningProcess, resourceId, numberOfUnit));
                currentRunningProcess.setStatusList(targetRCB.getWatingList());
                if(!isAutomatedMode)
                    System.out.print("Process " + currentRunningProcess.getId() + " is blocked; ");
                scheduler();
            }

        }catch(NullPointerException ex) {

            SystemUtility.getInstance().displayMessage(isAutomatedMode,
                    "ERROR@Manager.requestResource() : Logical Fallacy");

        }

    }

    /** Refactoring is possible do it later*/
    @Override
    public void releaseResource(String resourceId, int numberOfUnit, PCB sourcePCB, boolean isCallFromDestroy) {

        RCB targetRCB = null;

        // Check current process is init process
        if(sourcePCB.getsPriority() == 0) {

            SystemUtility.getInstance().displayMessage(isAutomatedMode,
                    "ERROR@Manger.releaseResource() : this process cannot release resource");

            return;
        }

        // Check resource name is valid
        if(!isValidResourceName(resourceId)) {
            SystemUtility.getInstance().displayMessage(isAutomatedMode,
                        "ERROR@Manager.releaseResource() : invalid resource name");
            return;
        }
        // Check current resource posses resource
        if(sourcePCB.getOtherResources().size() == 0) {
            SystemUtility.getInstance().displayMessage(isAutomatedMode,
                    "ERROR@Manager.releaseResource() : currently has no resource");

            return;
        }

        // Check target resource exist in PCB's otherResource
        if(!doesPcbHasTargetResource(sourcePCB, resourceId)) {

            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("error(not holding resource: ")
                    .append(parseResourceNumberFromResourceId(resourceId)).append(")");
            SystemUtility.getInstance().displayMessage(isAutomatedMode, errorMessage.toString());

            return;

        }

        // Resource Bundle is exist in PCB.otherResources, so now time to retrieve
        targetRCB = retrieveRCBFromAvailableResources(resourceId); // Retrieve RCB

        // if released amount is bigger than the resource size, display error
        if(numberOfUnit > targetRCB.getNumberOfAllocatedUnit() ) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("error(release too many units: ")
                    .append(numberOfUnit).append("/").append(targetRCB.getResourceID()).append(":")
                    .append(targetRCB.getNumberOfAllocatedUnit()).append(")");

            SystemUtility.getInstance().displayMessage(isAutomatedMode,  errorMessage.toString());

            return;
        }

        // Retrieve Bundle
        ResourceBundle targetResourceBundle = findBundle(resourceId, sourcePCB);

        // Retrieve bundle test to prevent critical error
        if(targetResourceBundle == null) {
            SystemUtility.getInstance().displayMessage(isAutomatedMode,
                    "ERROR@Manager.releaseResource() : sourcePCB has no bundle");
            return;
        }

        /*** CHECKPOINT: check logic of below codes and OUTPUT ***/
        WaitingList wl = (WaitingList)targetRCB.getWatingList(); // retrieve waiting list
        int loopCounter = wl.size();

        /* Case1 :Resource can be fully release */
        if(targetRCB.getNumberOfAllocatedUnit() == numberOfUnit) {
            targetRCB.release(numberOfUnit);
            sourcePCB.getOtherResources().remove(targetResourceBundle);

            int availableResourceUnits = targetRCB.getNumberOfFreeUnit(); // retrieve number of free units

            /* * * *
             * Iterate WaitingList
             */
            for(int i = 0; i < loopCounter;i++) {

                WaitingBundle bundle = (WaitingBundle)wl.get(0);

                if(availableResourceUnits >= bundle.getNumberOfDemand()) {

                    availableResourceUnits -= bundle.getNumberOfDemand(); // recompute # of available resource unit

                    wl.remove(bundle); // remove current waiting bundle from waiting list

                    // Change PCB's state
                    PCB targetPCB = bundle.getOwnerOfBundle();
                    targetPCB.changeState(ReadyState.getInstance());
                    readyList.add(targetPCB);
                    targetPCB.setStatusList(readyList);

                    // Allocate resource to PCB
                    targetPCB.getOtherResources().add(new ResourceBundle(targetRCB.getResourceID(), bundle.getNumberOfDemand()));
                    targetPCB.setStatusList(readyList);
                    targetRCB.request(bundle.getNumberOfDemand());


                }

            }



        }

        /* Case2 : partially release */
        else {
            targetRCB.release(numberOfUnit);
            targetResourceBundle.setNumberOfUnit(targetResourceBundle.getNumberOfUnit() - numberOfUnit); // update targetResourceBundle carried by process

            int availableResourceUnits = targetRCB.getNumberOfFreeUnit();

            // Check waiting list
            if(wl.size() > 0) { // If statement is not necessary, but just use it for safe

                /* * * *
                 * Iterate WaitingList
                 */
                for(int i = 0; i < loopCounter;i++ ) {

                    WaitingBundle waitingBundle = wl.get(0);

                    if(availableResourceUnits  >= waitingBundle.getNumberOfDemand()) {

                        availableResourceUnits -= waitingBundle.getNumberOfDemand(); // recompute # of available resource unit

                        wl.remove(waitingBundle); // remove current waiting bundle from waiting list

                        // Change PCB's state
                        PCB targetPCB =  waitingBundle.getOwnerOfBundle();
                        targetPCB.changeState(ReadyState.getInstance());
                        readyList.add(targetPCB);
                        targetPCB.setStatusList(readyList);

                        // Allocate resource to PCB
                        targetPCB.getOtherResources().add(new ResourceBundle(targetRCB.getResourceID(), waitingBundle.getNumberOfDemand()));
                        targetPCB.setStatusList(readyList);
                        targetRCB.request(waitingBundle.getNumberOfDemand());


                    }else {
                        break;
                    }
                } // END of for loop

            } // End of if statement
        }// End of else statment


        if(!isCallFromDestroy) {
           scheduler();
        }

    }

    @Override
    public void doTimeOut() {
        readyList.add(currentRunningProcess);
        currentRunningProcess.changeState(ReadyState.getInstance());
        scheduler();

    }

    private void scheduler() {
        // Step 1. find highest priority process
        PCB highestCandidateProcess = readyList.findHighestPriorityPCB();

        // Step 2. compare the process with currently running process
        if(currentRunningProcess == null) {
            preempt(highestCandidateProcess, false);
        }
        else if(highestCandidateProcess.getsPriority() > currentRunningProcess.getsPriority() ||
                !currentRunningProcess.getStatusType().equals(RunningState.getInstance())) {


            /* Don't understand TA's sudden e-mail, but to follow his requirement,
             * I just these code .
             */

            /** **/

            preempt(highestCandidateProcess, true);
        } else {
            try {
                displayReplyMessage(currentRunningProcess);
            }catch(IOException ioEx) {
                ioEx.printStackTrace();
                System.err.println("Point : SNIPER");
            }
        }

    }

    private void preempt(PCB pcb, boolean isHighPriorityAppeared) {

        // To cover the init,
        if(currentRunningProcess == null) {
            currentRunningProcess = pcb;
        }

        if(currentRunningProcess.getStatusType().equals(RunningState.getInstance())) {
            currentRunningProcess.changeState(ReadyState.getInstance());
            if(isHighPriorityAppeared)
                readyList.add(currentRunningProcess, 0);
            else
                readyList.add(currentRunningProcess);
        }

        pcb.changeState(RunningState.getInstance());
        currentRunningProcess = pcb;
        readyList.remove(pcb);
        try {
            displayReplyMessage(pcb);
        }catch(IOException ioEx) {
            ioEx.printStackTrace();
            System.err.println("HACKER");
        }
    }

    public PCB getCurrentRunningProcess() {
        return currentRunningProcess;
    }

    /* * *
     *  Private Component
     */

    private void initilizeResource() {
        availableResources = new LinkedList<RCB>();
        availableResources.add(new RCB("R1", 1));
        availableResources.add(new RCB("R2", 2));
        availableResources.add(new RCB("R3", 3));
        availableResources.add(new RCB("R4", 4));
    }

    private ResourceBundle findBundle(String resourceId, PCB sourceProcess) {
        for(ResourceBundle bundle : sourceProcess.getOtherResources()) {
            if(bundle.getResourceId().equals(resourceId)) {
                return bundle;
            }
        }

        return null;
    }

    private boolean isProcessExist(PCB parent, String processId) {
        List<PCB> children = parent.getChildrenFromTree();
        boolean result = false;

        for(PCB current : children) {
            if(current.getId().equals(processId)) {
                return true;
            }

            if(current.getChildrenFromTree().size() > 0) {
                // NOTE : recursion here. Easy to make a mistake. Need to retrieve result from recursive method.
                result = isProcessExist(current, processId);
            }
        }

        return result;
    }

    private void traverse(PCB parent) {

        List<PCB> children = parent.getChildrenFromTree();

        for(PCB current : children) {
            System.out.println(current.getId());
            if(current.getChildrenFromTree().size() > 0) {
                traverse(current);
            }
        }

    }

    private PCB retrievePCBFromTree(PCB parent, String targetProcessId) {

        List<PCB> children = parent.getChildrenFromTree();
        PCB target = null;

        for(PCB current : children) {

            if(current.getId().equals(targetProcessId)) {

                return current;
            }

            if(current.getChildrenFromTree().size() > 0) {
                // NOTE : recursion here. Easy to make a mistake. Need to retrieve result from recursive method.
                target = retrievePCBFromTree(current, targetProcessId);
            }
        }

        return target;
    }

    private boolean doesPcbHasTargetResource(PCB pcb, String resourceId) {
        for(ResourceBundle bundle : pcb.getOtherResources() ) {
            if(bundle.getResourceId().equals(resourceId)) {
                return true;
            }
        }
        return false;
    }

    private RCB retrieveRCBFromAvailableResources(String resourceId) {

        for(RCB rcb : availableResources) {
            if( rcb.getResourceID().equals(resourceId) ) {
                return rcb;
            }
        }

        return null;
    }

    private boolean isValidResourceName(String resourceId) {
        for(RCB rcb : availableResources) {
            if(rcb.getResourceID().equals(resourceId))
                return true;
        }

        return false;
    }


    private void setMode(boolean mode) {

        isAutomatedMode = mode;

    }

    private int parseResourceNumberFromResourceId(String resourceId) {
        int resourceNumber = -1;

        StringBuilder resourceNumberStrBuilder = new StringBuilder();

        for(int i = 0; i < resourceId.length(); i++) {
            char ch = resourceId.charAt(i);
            if (ch > 47 && ch < 58) {
                resourceNumberStrBuilder.append(ch);
            }
        }

        try {
            resourceNumber = Integer.parseInt(resourceNumberStrBuilder.toString());
        }catch(NumberFormatException exNumberFormat) {
            exNumberFormat.printStackTrace();
        }

        return resourceNumber;
    }

    /* *
     *  Message
     */
    private void displayReplyMessage(PCB pcb) throws IOException {
        if(isAutomatedMode) {
            SystemUtility.getInstance().displayMessage(isAutomatedMode, (pcb.getId() + " "));
        } else {
            System.out.printf("Process %s is running\n", pcb.getId());
        }

    }

    public boolean isAutomatedMode() {
        return isAutomatedMode;
    }

    /* *
     *  For Debug
     */
    public void displayCurrentRunningProcessStatus() {
        if(currentRunningProcess != null) {
            System.out.println("------- Current running process information -------");
            System.out.println("Current Running Process id : " + currentRunningProcess.getId());
            System.out.println("Current Running Process Status : " + currentRunningProcess.getStatusType());
            System.out.println("is it Leaf? : " + currentRunningProcess.isLeafProcess());
            System.out.println("------ Children -------");
            for(PCB child : currentRunningProcess.getChildrenFromTree()) {
                System.out.println(child.getId());
            }
            System.out.println("------- Bundle Information -------");
            if (currentRunningProcess.getOtherResources().size() == 0)
                System.out.println("NO BUNDLE EXIST");
            for (ResourceBundle bundle : currentRunningProcess.getOtherResources()) {
                System.out.println(bundle.toString());
            }
            System.out.println("------- End of running process information -------");
        } else {
            System.out.println("CurrentProcess is null");
        }

    }

    public void displayRootProcess() {
        System.out.println("------- Root process information -------");
        System.out.println("Name of root process : " + rootPCB.toString() );
        System.out.println("------- Root process info End -------");
        System.out.println();
    }

    public void displayReadyList() {
        readyList.displayAll();
    }

    public void displayTree() {
        traverse(rootPCB);
    }

    public void displayAvailableResourceInformation() {
        for(RCB resource : availableResources) {
            System.out.println("Size of Waiting List : " + resource.getWatingList().size());
            System.out.print(resource.getResourceID() + " ::: free[" + resource.getNumberOfFreeUnit()
                    + "], allocated[" + resource.getNumberOfAllocatedUnit() + "], waitingList : ");

            Iterator it = resource.getWatingList().iterator();

            while(it.hasNext()) {
                WaitingBundle bundle = (WaitingBundle)it.next();

                System.out.print( bundle.getOwnerOfBundle() );
            }

            System.out.println();


        }
    }

    public void findHighestPriorityProcessFromReadyList() {

       // System.out.println(readyList.findHighestPriorityPCB().toString() ) ;

        displayAvailableResourceInformation();
    }





}
