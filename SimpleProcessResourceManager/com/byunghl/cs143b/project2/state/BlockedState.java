package com.byunghl.cs143b.project2.state;

/**
 * Created by aznnobless on 2/3/15.
 */
public class BlockedState implements State {

    private static BlockedState ourInstance = new BlockedState();

    private BlockedState() {

    }



    public static BlockedState getInstance() {

        return ourInstance;
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
        return "blocked";
    }

    @Override
    public String toString() {
        return getStateName();
    }
}
