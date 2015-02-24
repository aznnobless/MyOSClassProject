package com.byunghl.cs143b.project2.command;

/**
 * Created by aznnobless on 2/11/15.
 */
public class CommentIgnoreCommand implements Command {
    @Override
    public boolean setArguments(String[] args) {
        return false;
    }

    @Override
    public void execute() {
        // comment line will be ignored do nothing.
    }
}
