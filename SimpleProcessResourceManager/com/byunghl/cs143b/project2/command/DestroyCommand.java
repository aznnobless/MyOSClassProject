package com.byunghl.cs143b.project2.command;

import com.byunghl.cs143b.project2.core.Manager;

/**
 * Created by aznnobless on 2/5/15.
 */
public class DestroyCommand implements Command {

    private Manager prManager;

    private String processName;

    public DestroyCommand(Manager m) {
        prManager = m;
    }

    @Override
    public boolean setArguments(String[] args) {

        processName = args[1];

        return true;
    }

    @Override
    public void execute() {
        prManager.destroyProcess(processName);
    }
}
