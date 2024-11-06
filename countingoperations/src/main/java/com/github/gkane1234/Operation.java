package com.github.gkane1234;

import java.util.function.DoubleBinaryOperator;

/**
    This class is used to represent operations.
    By default, it uses the standard 4 operations: addition, subtraction, multiplication, and division.
*/
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

    /**
        Constructor for the Operation class.
        @param operationFunction: a <code>DoubleBinaryOperator</code> representing the operation function.
        @param commutative: a <code>boolean</code> representing whether the operation is commutative.
        @param name: a <code>char</code> representing the name of the operation.
    */
    public Operation(DoubleBinaryOperator operationFunction, boolean commutative,char name) {
        this.name=name;
        this.commutative = commutative;
        this.operationFunction = operationFunction;
    }
    /**
        Applies the operation on two numbers.
        @param a: a <code>double</code> representing the first number.
        @param b: a <code>double</code> representing the second number.
        @return a <code>double</code> representing the result of the operation.
    */
    public double apply(double a, double b) {
        return operationFunction.applyAsDouble(a, b);
    }
    /**
        Returns whether the operation is commutative.
        @return a <code>boolean</code> representing whether the operation is commutative.
    */
    public boolean isCommutative(){
        return this.commutative;
    }
    /**
        Returns the name of the operation.
        @return a <code>char</code> representing the name of the operation.
    */
    public char getName(){
        return this.name;
    }

    @Override
    public String toString() {
        return String.valueOf(this.name);
    }

    /**
        Changes the operations to a new set of operations.
        @param newOperations: an <code>Operation[]</code> representing the new operations.
    */
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

    /**
        Returns the current set of operations.
        @return an <code>Operation[]</code> representing the current operations.
    */
    public static Operation[] getOperations() {
        return OPERATIONS;
    }
    /**
        Returns the naive number of unique possible orderings of the operations, taking commutativity into account.
        @return an <code>int</code> representing the naive number of possible orderings of the operations.
    */
    public static int getNumOperationOrderings() {
        
        return NUM_OPERATION_ORDERINGS;
    }
}