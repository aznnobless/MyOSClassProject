package com.byunghl.cs143b.project2.state;

public class RunningState implements State {

    private static RunningState ourInstance = new RunningState();

    public static RunningState getInstance() {
        return ourInstance;
    }

    private RunningState() {
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
        return "running";
    }

    @Override
    public String toString() {
        return getStateName();
    }
}
