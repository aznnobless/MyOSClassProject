package com.byunghl.cs143b.project2.command;

/**
 * Created by aznnobless on 2/5/15.
 */
public interface Command {

    public abstract boolean setArguments(String[] args);
    public abstract void execute();
}
