package com.github.gkane1234;

public class SolutionList extends EvaluatedExpressionList{

    private double goal;

    public SolutionList(double[] values, double goal) {
        super(values);
        this.goal=goal;
    }
    public SolutionList(int[] values, double goal) {
        super(values);
        this.goal=goal;
    }

    public SolutionList(EvaluatedExpressionList evaluatedExpressionList, double goal) {
        this(evaluatedExpressionList.getValues(),goal);
        for (EvaluatedExpression evaluatedExpression : evaluatedExpressionList.getEvaluatedExpressionList()) {
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
