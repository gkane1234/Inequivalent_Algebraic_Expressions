package com.github.gkane1234;
/**
    An interface for listening to updates from the solver.
    Used for the applet.
*/
public interface SolverUpdateListener {
    /**
        Called when the solver has an update to report.
        @param update: a <code>String</code> representing the update message from the solver.
    */
    void onSolverUpdate(String update);
}
