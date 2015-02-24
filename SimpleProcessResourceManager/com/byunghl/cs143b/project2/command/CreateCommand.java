package com.byunghl.cs143b.project2.command;

import com.byunghl.cs143b.project2.core.Manager;

/**
 * Created by aznnobless on 2/5/15.
 */
public class CreateCommand implements Command {

    private Manager prManager;
    private String processName;
    private int priority;

    public CreateCommand(Manager m) {
        prManager = m;
    }


    @Override
    public boolean setArguments(String[] args) {

        processName = args[1];

        boolean accomplished = true;
        try {
            priority = Integer.parseInt(args[2]);
        }catch(NumberFormatException ex) {
            accomplished = false;
            ex.printStackTrace();
            System.err.println("Invalid arguments are passed to CreateCommand.setArguments()");
        }

        return accomplished;
    }

    @Override
    public void execute() {
        prManager.createProcess(processName, priority);
    }
}
