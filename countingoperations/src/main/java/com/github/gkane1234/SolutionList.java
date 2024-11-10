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

    public SolutionList(EvaluatedExpressionList EvaluatedExpressionList, double goal) {
        this(EvaluatedExpressionList.getValues(),goal);
        for (EvaluatedExpression evaluatedExpression : EvaluatedExpressionList.getEvaluatedExpressionList()) {
            this.addEvaluatedExpression(evaluatedExpression);
        }
    }

    public void addSolution(EvaluatedExpression toAdd) {
        if (toAdd.getValue()!=this.goal) {
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
