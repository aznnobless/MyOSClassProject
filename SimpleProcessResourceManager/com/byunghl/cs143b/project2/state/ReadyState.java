package com.byunghl.cs143b.project2.state;

/**
 * Created by aznnobless on 2/3/15.
 */
public class ReadyState implements State {

    private static State ourInstance = new ReadyState();


    public static State getInstance() {
        return ourInstance;
    }

    private ReadyState() {

    }

    @Override
    public void updateProcessState(Context context, String command) {

    }

    @Override
    public void destroyProcess(Context context) {
        context.changeState(null);
    }

    @Override
    public String getStateName() {
        return "ready";
    }

    @Override
    public String toString() {
        return getStateName();
    }
}
