package com.byunghl.cs143b.project2.command;

import com.byunghl.cs143b.project2.core.Manager;

/**
 * Created by aznnobless on 2/6/15.
 */
public class HighestPriorityFinderCommand implements Command {

    private Manager prManager;

    public HighestPriorityFinderCommand(Manager prManager) {
        this.prManager = prManager;
    }

    @Override
    public boolean setArguments(String[] args) {
        return false;
    }

    @Override
    public void execute() {
        prManager.findHighestPriorityProcessFromReadyList();
    }
}
