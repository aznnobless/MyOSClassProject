package com.byunghl.cs143b.project2.command;

/**
 * Created by aznnobless on 2/5/15.
 */
public class CommandInvoker {

    private Command command;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void execute() {
        command.execute();
    }



}
