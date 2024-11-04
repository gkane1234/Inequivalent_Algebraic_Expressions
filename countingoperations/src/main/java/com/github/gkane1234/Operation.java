package com.github.gkane1234;

import java.util.function.DoubleBinaryOperator;

public class Operation {
    private static Operation[] OPERATIONS = new Operation[] {
        new Operation((a, b) -> a + b, true,'+'),
        new Operation((a, b) -> a - b, false,'-'),
        new Operation((a, b) -> a * b, true,'*'),
        new Operation((a, b) -> (b != 0) ? a / b : Double.NaN, false,'/')};

    private static int NUM_OPERATION_ORDERINGS = 6;

    private DoubleBinaryOperator operationFunction;
    private boolean commutative;
    private char name;

    public Operation(DoubleBinaryOperator operationFunction, boolean commutative,char name) {
        this.name=name;
        this.commutative = commutative;
        this.operationFunction = operationFunction;
    }

    public double apply(double a, double b) {
        return operationFunction.applyAsDouble(a, b);
    }
    public Boolean isCommutative(){
        return this.commutative;
    }
    public char getName(){
        return this.name;
    }

    @Override
    public String toString() {
        return String.valueOf(this.name);
    }


    public static void changeOperations(Operation[] newOperations) {
        OPERATIONS=newOperations;
        int operationOrderings = 0;
        for (Operation operation: OPERATIONS) {
            if (operation.commutative) {
                operationOrderings++;
            } else {
                operationOrderings += 2;
            }
        }
        NUM_OPERATION_ORDERINGS = operationOrderings;
    }

    public static Operation[] getOperations() {
        return OPERATIONS;
    }

    public static int getNumOperationOrderings() {
        /*
        Returns the naive number of possible orderings of the operations, taking commutativity into account.

        Used in Expression.createCombinedExpressions()
        */
        return NUM_OPERATION_ORDERINGS;
    }
}