package com.github.gkane1234;

public class SolutionList extends EvaluatedExpressionSet{

    private double goal;

    public SolutionList(double[] values, double goal) {
        super(values);
        this.goal=goal;
    }
    public SolutionList(int[] values, double goal) {
        super(values);
        this.goal=goal;
    }

    public SolutionList(EvaluatedExpressionSet evaluatedExpressionSet, double goal) {
        this(evaluatedExpressionSet.getValues(),goal);
        for (EvaluatedExpression evaluatedExpression : evaluatedExpressionSet.getEvaluatedExpressionList()) {
            this.addEvaluatedExpression(evaluatedExpression);
        }
    }
    @Override
    public void addEvaluatedExpression(EvaluatedExpression toAdd) {
        if (!Solver.equal(toAdd.getValue(),this.goal)) {
            throw new IllegalArgumentException("Goal of solution does not match goal of solution list");
        }

        super.addEvaluatedExpression(toAdd);
    }

    public int getNumSolutions() {
        return super.getNumEvaluatedExpressions();
    }

    public double getGoal() {
        return this.goal;
    }


}
