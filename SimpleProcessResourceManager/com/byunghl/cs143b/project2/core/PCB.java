package com.byunghl.cs143b.project2.core;

import com.byunghl.cs143b.project2.interfaces.PrivateList;
import com.byunghl.cs143b.project2.state.Context;
import com.byunghl.cs143b.project2.state.ReadyState;
import com.byunghl.cs143b.project2.state.State;

import java.util.LinkedList;
import java.util.List;


/**
 * com.byunghl.cs143b.project2.core.PCB(Process descriptor) is process control block which represent each process.
 *
 * com.byunghl.cs143b.project2.core.PCB is constructed at process creation and represents the process during its existence.
 * PCBs are sued and modified by basic process and resource operations, interrupt routines, and schedulers.
 *
 * Each com.byunghl.cs143b.project2.core.PCB is created dynamically using the operation create.
 * Each com.byunghl.cs143b.project2.core.PCB is destroyed dynamically using the operation destroy.
 * The only exception is the special Init process, whose com.byunghl.cs143b.project2.core.PCB is created automatically when the system starts up
 * and destroyed when the system is shutdown.
 */

public class PCB implements Context{

    private String id;
    private int sPriority;
    private List<ResourceBundle> otherResources; // each of link point resource block
    private Status status;
    private CreationTree creationTree;

    // Default Constructor
    private PCB(){
        this.status = new Status();
    }

    public PCB(String processId, int priority) {
        this();
        this.id = processId;
        this.sPriority = priority;
        this.otherResources = new LinkedList<ResourceBundle>();
        this.status = new Status();
        this.creationTree = new CreationTree();
    }

    /* * * * * * * * * * *
     * Getters and Setters
     * * * * * * * * * * */

    // Getter
    public String getId() {
        return id;
    }

    // Getter
    public List<ResourceBundle> getOtherResources() {
        return otherResources;
    }

    // Getter
    public CreationTree getCreatetionTree() {
        return creationTree;
    }

    // Getter
    public int getsPriority() {
        return sPriority;
    }

    // Getter
    public State getStatusType() {
        return status.getType();
    }

    // Getter
    public PrivateList getStatusList() {
        return status.getList();
    }

    // Setter
    public void setStatusList(PrivateList list) {
        status.setList(list);
    }

    // Getter
    public PCB getParentFromTree() {
        return creationTree.getParent();
    }

    // Getter
    public List<PCB> getChildrenFromTree() {
        return creationTree.getChildren();
    }

    public void addChildToTree(PCB pcb) {
        creationTree.addChildren(pcb);
    }

    public void setParentToTree(PCB pcb) {
        creationTree.setParent(pcb);
    }

    public void removeChildFromTree(PCB pcb) {
        creationTree.removeChildren(pcb);
    }

    public boolean isLeafProcess() {
        return creationTree.isLeaf();
    }
    @Override
    public void changeState(State state) {
        status.setType(state);
    }

    @Override
    public String toString() {
        return id;
    }



    /* Inner class : Status
     *
     * The status of a process p may be described by a structure with two components: Status.Type and Status.List.
     * The Status.Type is one of ready, running, or blocked.
     *
     */

    private class Status{

        private State type; // Status.type

        // Status.List
        /* The field list points to one of several possible lists on which the process may reside.
         *
         * When the process is running or ready to run, it has an entry on the Ready List of the process scheduler.
         *
         * When Process blocks on a resource,
         * this entry is moved from the Ready List to Waiting List associated with that resource
         *
         * When the process acquires the resource, its entry is moved back to the Ready List.
         * The field Status.List points to either the Ready List or one of the Waiting Lists, depending on the
         * process status.
         *
         */

        private PrivateList list; // Status.list

        // Default constructor
        public Status() {
            this.type = ReadyState.getInstance();
            this.list = null;
        }

        // Getter
        public State getType() {
            return type;
        }

        // Setter
        public void setType(State type) {
            this.type = type;
        }

        // Getter
        public PrivateList getList() {
            return list;
        }

        // Setter
        public void setList(PrivateList list) {
            this.list = list;
        }

        public boolean isParent(PCB target) {

            return creationTree.getParent().equals(target);

        }



        // Getter
        ///private CreationTree get

    }

    /* Inner class : CreationTree
     *
     * CreationTree consists of two sub-fields: CreationTree.parent and CreationTree.children
     * CreationTree is non-binary tree.
     */
    private class CreationTree {

        public PCB parent;
        public List<PCB> children;

        public CreationTree() {
            parent = null;
            children = new LinkedList<PCB>();
        }

        public String toString(){
            return "Parent: " + parent.getId() + ", Children: " + children.toString();
        }

        // getter
        public PCB getParent() {
            return parent;
        }

        // getter
        public List<PCB> getChildren() {
            return children;
        }

        // setter
        public void setParent(PCB target) {
            this.parent = target;
        }

        public void addChildren(PCB pcb) {
            children.add(pcb);
        }

        public void removeChildren(PCB pcb) {
            children.remove(pcb);
        }

        public boolean isLeaf() {
            return children.isEmpty();
        }

    }

}

