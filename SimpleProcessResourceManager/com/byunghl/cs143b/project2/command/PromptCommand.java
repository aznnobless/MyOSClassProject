package com.byunghl.cs143b.project2.command;

import com.byunghl.cs143b.project2.core.Manager;
import com.byunghl.cs143b.project2.utility.SystemUtility;

import java.io.BufferedWriter;
import java.io.IOException;

public class PromptCommand implements Command {

    private Manager prManager;
    private BufferedWriter bw;

    public PromptCommand(Manager prManager, BufferedWriter bw) {
        this.prManager = prManager;
        this.bw = bw;
    }

    @Override
    public boolean setArguments(String[] args) {
        return false;
    }

    @Override
    public void execute() {
        if(prManager.isAutomatedMode()) {
            try {
                SystemUtility.getInstance().endCycle(prManager.isAutomatedMode(), bw);
                bw.newLine();
            }catch(IOException ex) {
                ex.printStackTrace();
                System.err.println("ERROR@PromptCommand");
            }
        }
    }
}
