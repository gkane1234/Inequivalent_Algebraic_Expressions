package com.github.gkane1234;

public interface SolverUpdateListener {
    /**
     * Called when the solver has an update to report
     * @param update The update message from the solver
     */
    void onSolverUpdate(String update);
}
