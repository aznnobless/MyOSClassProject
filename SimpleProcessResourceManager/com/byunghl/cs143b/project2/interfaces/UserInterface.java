package com.byunghl.cs143b.project2.interfaces;

import com.byunghl.cs143b.project2.core.PCB;

public interface UserInterface {

    abstract public void init(boolean mode);
    abstract public void quit();

    abstract public void createProcess(String processName, int priority);
    abstract public void destroyProcess(String processName);
    abstract public void requestResource(String resrcName, int numberOfUnit);
    abstract public void releaseResource(String resrcName, int numberOfUnit,PCB sourcePCB, boolean isCallFromDestroy);
    abstract public void doTimeOut();

}
