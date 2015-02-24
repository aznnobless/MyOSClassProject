package com.byunghl.cs143b.project2.core;

import com.byunghl.cs143b.project2.interfaces.PrivateList;
import com.byunghl.cs143b.project2.utility.SystemUtility;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ReadyList implements PrivateList {

    /**
     * NOTE:
     *  Because of destroy function supported by manager, Queue is not appropriate data structure.
     *  Just use LinkedList
     */

    private List<PCB> highestPriority;
    private List<PCB> middlePriority;
    private List<PCB> lowestPriority;

    public ReadyList() {
        highestPriority = new LinkedList<PCB>();
        middlePriority = new LinkedList<PCB>();
        lowestPriority = new LinkedList<PCB>();
    }

    @Override
    public void add(Object element) {

        PCB temp = (PCB)element;

        switch(temp.getsPriority()) {
            case 0:
                lowestPriority.add((PCB)element);
                break;
            case 1:
                middlePriority.add((PCB)element);
                break;
            case 2:
                highestPriority.add((PCB)element);
                break;
            default:
                SystemUtility.getInstance().displayMessage(false,
                        "Error@com.byunghl.cs143b.project2.core.ReadyList.enqueue() : Something is wrong");
        }
    }

    @Override
    public void add(Object element, int index) {
        PCB temp = (PCB)element;

        switch(temp.getsPriority()) {
            case 0:
                lowestPriority.add(index, temp);
                break;
            case 1:
                middlePriority.add(index, temp);
                break;
            case 2:
                highestPriority.add(index, temp);
                break;
            default:
                SystemUtility.getInstance().displayMessage(false,
                        "Error@com.byunghl.cs143b.project2.core.ReadyList.enqueue() : Something is wrong");
        }
    }

    @Override
    public void remove(Object element) {

        PCB temp = (PCB)element;

        switch(temp.getsPriority()) {

            case 0:

                lowestPriority.remove((PCB)element);

                //SystemUtility.displayErrorMessage(false, "Error@ReadyList.remove() case 0");
                break;
            case 1:

                middlePriority.remove((PCB)element);

                // SystemUtility.displayErrorMessage(false, "Error@ReadyList.remove() case 1");
                break;
            case 2:

                highestPriority.remove((PCB)element);

                //    SystemUtility.displayErrorMessage(false, "Error@ReadyList.remove() case 2");
                break;
            default:
                SystemUtility.getInstance().displayMessage(false,
                        "Error@com.byunghl.cs143b.project2.core.ReadyList.dequeue() : Something is wrong");

        }
    }

    @Override
    public Object get(int index) {
        return null;
    }

    @Override
    public int size() {
        return highestPriority.size() + middlePriority.size() + lowestPriority.size();
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    public boolean contains(PCB pcb, int priotity) {

        if(priotity <=0 && priotity > 2 ) {
            SystemUtility.getInstance().displayMessage(false, "Error@ReadyList.contains() : invalid priority");
        }

        if(priotity == 1)
            return middlePriority.contains(pcb);
        else
            return highestPriority.contains(pcb);
    }

    public PCB findHighestPriorityPCB() {
        if(!highestPriority.isEmpty()) {
            return highestPriority.get(0);
        } else if(!middlePriority.isEmpty()) {
            return middlePriority.get(0);
        } else {
            ///System.out.println("REACHEd : " + lowestPriority.get(0).getId());
            return lowestPriority.get(0);
        }
    }

    public void displayAll() {

        System.out.print("Highest Priority List: ");
        for(PCB item : highestPriority) {
            System.out.print(item.toString() + ", ");
        }

        System.out.println();

        System.out.print("Middle Priority List: ");
        for(PCB item : middlePriority) {
            System.out.print(item.toString() + ", ");
        }
        System.out.println();

        System.out.print("Lowest Priority List: ");
        for(PCB item : lowestPriority) {
            System.out.print(item.toString() + ", ");
        }

        System.out.println();

    }



}
