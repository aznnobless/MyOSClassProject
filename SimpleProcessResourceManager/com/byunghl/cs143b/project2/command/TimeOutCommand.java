package com.byunghl.cs143b.project2.command;

import com.byunghl.cs143b.project2.core.Manager;

/**
 * Created by aznnobless on 2/5/15.
 */
public class TimeOutCommand implements Command{

    private Manager prManager;

    public TimeOutCommand(Manager prManager) {
        this.prManager = prManager;
    }

    @Override
    public boolean setArguments(String[] args) {
        return true;
    }

    @Override
    public void execute() {
        prManager.doTimeOut();
    }
}
