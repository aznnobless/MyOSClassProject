package com.byunghl.cs143b.project2.command;

import com.byunghl.cs143b.project2.core.Manager;

/**
 * Created by aznnobless on 2/5/15.
 */
public class ReleaseCommand implements Command {

    private Manager prManager;
    private String resourceName;
    private int numberOfUnit;

    public ReleaseCommand(Manager m) {
        prManager = m;
    }

    @Override
    public boolean setArguments(String[] args) {
        resourceName = args[1];
        boolean accomplished = true;
        try {
            numberOfUnit = Integer.parseInt(args[2]);
        } catch(NumberFormatException ex) {
            accomplished = false;
        }

        return accomplished;
    }

    @Override
    public void execute() {
        prManager.releaseResource(resourceName, numberOfUnit, prManager.getCurrentRunningProcess(),false);

    }
}
