package com.github.gkane1234;

/**
    This class is used to represent a solution to a goal.

    This does not verify that the solution is correct, it is only a way to store a solution.
*/
public class Solution{
    private static final int DECIMAL_PLACES = 3;
    private double[] values;
    private double goal;
    private Expression expression;
    /**
        Constructor for the Solution class.
        @param expression: an <code>Expression</code> representing the expression used to get the solution.
        @param values: a <code>double[]</code> representing the values used in the expression.
        @param goal: a <code>double</code> representing the goal of the solution.
    */
    public Solution(Expression expression, double[] values,double goal) {
        this.expression=expression;
        this.values=values;
        this.goal = goal;
    }
    /**
        Displays the solution as a string.
        @return a <code>String</code> representing the solution.
    */
    public String display() {
        ArrayStack<String> stack = new ArrayStack<>(expression.order.length);
        byte values_pointer =0;
        byte operations_pointer = 0;
        for (boolean isNumber : this.expression.order) {
            if (isNumber) {
                double next = this.values[this.expression.valueOrder[values_pointer++]];
                if (next == Math.round(next)) {
                    stack.push(String.valueOf(Math.round(next)));
                } else {
                    stack.push(String.format("%." + DECIMAL_PLACES + "f", next));;
                }
            } else {
                byte opCode = expression.operations[operations_pointer++];
                String b = stack.pop();
                String a = stack.pop();
                String combinedExpression = "("+a+String.valueOf(Operation.getOperations()[opCode])+b+")";
                stack.push(combinedExpression);
            }   
        }
        return stack.peek();
    }
    /**
        Returns the values used in the expression.
        @return a <code>double[]</code> representing the values used in the expression.
    */
    public double[] getValues() {
        return this.values;
    }
    /**
        Returns the goal of the solution.
        @return a <code>double</code> representing the goal of the solution.
    */
    public double getGoal() {
        return this.goal;
    }
    /**
        Returns a string representation of the solution.
        @return a <code>String</code> representing the solution.
    */
    @Override
    public String toString() {
        return this.display();
    }

}
