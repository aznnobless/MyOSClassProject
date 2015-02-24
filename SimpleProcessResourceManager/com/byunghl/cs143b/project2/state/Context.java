package com.byunghl.cs143b.project2.state;

/**
 * Context interface manages state.
 */
public interface Context {

    public abstract void changeState(State state);

}
