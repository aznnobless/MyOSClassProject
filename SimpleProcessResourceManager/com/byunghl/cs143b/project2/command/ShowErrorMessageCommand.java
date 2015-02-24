package com.byunghl.cs143b.project2.command;

import com.byunghl.cs143b.project2.core.Manager;
import com.byunghl.cs143b.project2.utility.SystemUtility;

import java.io.BufferedWriter;

public class ShowErrorMessageCommand implements Command {
    private Manager prManager;
    private BufferedWriter bw;

    public ShowErrorMessageCommand(Manager prManager) {
        this.prManager = prManager;

    }

    @Override
    public boolean setArguments(String[] args) {
        return false;
    }

    @Override
    public void execute() {

        String errorMassage = "error(Invalid command)";

        if(prManager.isAutomatedMode())
            SystemUtility.getInstance().displayMessage(prManager.isAutomatedMode(), errorMassage);
        else
            System.out.println(errorMassage);


    }
}
