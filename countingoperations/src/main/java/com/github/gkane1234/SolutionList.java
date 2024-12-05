package com.github.gkane1234;

/**
    A list of evaluated expressions that are solutions to a goal.
*/
public class SolutionList extends EvaluatedExpressionList{

    private double goal;

    /**
        Constructor for a SolutionList.
        @param values: a <code>double[]</code> representing the values of the solution list.
        @param goal: a <code>double</code> representing the goal of the solution list.
    */
    public SolutionList(double[] values, double goal) {
        super(values);
        this.goal=goal;
    }
    /**
        Constructor for a SolutionList.
        @param values: a <code>int[]</code> representing the values of the solution list.
        @param goal: a <code>double</code> representing the goal of the solution list.
    */
    public SolutionList(int[] values, double goal) {
        super(values);
        this.goal=goal;
    }

    /**
        Constructor for a SolutionList.
        @param evaluatedExpressionList: an <code>EvaluatedExpressionList</code> representing the evaluated expression list to add to the solution list.
        @param goal: a <code>double</code> representing the goal of the solution list.
    */
    public SolutionList(EvaluatedExpressionList evaluatedExpressionList, double goal) {
        this(evaluatedExpressionList.getValues(),goal);
        for (EvaluatedExpression evaluatedExpression : evaluatedExpressionList.getEvaluatedExpressionList()) {
            this.addEvaluatedExpression(evaluatedExpression);
        }
    }
    /**
        Adds an evaluated expression to the solution list.
        @param toAdd: an <code>EvaluatedExpression</code> representing the evaluated expression to add to the solution list.
    */
    @Override
    public void addEvaluatedExpression(EvaluatedExpression toAdd) {
        if (!Solver.equal(toAdd.getValue(),this.goal)) {
            throw new IllegalArgumentException("Goal of solution does not match goal of solution list");
        }

        super.addEvaluatedExpression(toAdd);
    }
    /**
        Returns the number of solutions in the solution list.
        @return an <code>int</code> representing the number of solutions in the solution list.
    */
    public int getNumSolutions() {
        return super.getNumEvaluatedExpressions();
    }
    /**
        Returns the goal of the solution list.
        @return a <code>double</code> representing the goal of the solution list.
    */
    public double getGoal() {
        return this.goal;
    }


}
