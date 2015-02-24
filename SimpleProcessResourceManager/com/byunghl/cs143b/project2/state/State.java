package com.byunghl.cs143b.project2.state;
/* * * *
 *
 */

public interface State {

  public abstract void updateProcessState(Context context, String command);

  public abstract void destroyProcess(Context context);

  public abstract String getStateName();

}
