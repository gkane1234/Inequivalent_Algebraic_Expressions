package com.github.gkane1234;
import java.util.*;

public class Expression {
    /*
    This class is used to represent an expression in Reverse Polish Notation (RPN) as a list of values and operations and an order.
    
    To evaluate the expression with specific values use the method evaluate_with_values.
    */
    public byte[] valueOrder;
    public byte[] operations;
    public boolean[] order;
    public Expression(byte[] valueOrder,byte[] operations,boolean[] order) {
        /*
        Constructor for the Expression class.
        value_order: the order that the values are evaluated in the expression.
        operations: the operations of the expression.
        order: the order of the expression. 

        For example: 
        The expression ((a+b)*c) is represented as value_order = [0,1,2] and operations = [0,2] and order = [true,true,false,true,false]
        To evaluate this expression with values a=3, b=4.7, c=5 the method evaluate_with_values would be used with values = [3,4.7,5].
        */

        this.valueOrder=valueOrder;
        this.operations=operations;
        this.order=order;
    }



    public Expression changeValueOrder(byte[] valueOrder) {
        /*
        Changes the value order of the expression.
        These values represent the order of the numbers in the expression.
        valueOrder: the new value order of the expression.
        */
        byte[] newValues = new byte[valueOrder.length];
        for (byte i=0;i<valueOrder.length;i++) {
            newValues[i]=valueOrder[this.valueOrder[i]];
        }

        return new Expression(newValues,this.operations,this.order);
    }

    public double evaluateWithValues(double[] values,int rounding) {
        /*
        Evaluates the expression with specific values.
        values: the values of the expression.
        rounding: the number of decimal places to round the result to.
        */
        double[] newValues = new double[values.length];
        for (byte i=0;i<valueOrder.length;i++) {
            newValues[i]=values[this.valueOrder[i]];
        }
        double val = this.evaluateRpn(newValues,rounding);

        return val;


        
    }

    @Override
    public String toString() {
        return Expression.convertToParenthetical(this);
    
    }



    private double evaluateRpn(double[] values,int rounding) {
        /*
        Evaluates the expression with specific values.
        values: the values of the expression.
        rounding: the number of decimal places to round the result to.
        */
        DoubleArrayStack stack = new DoubleArrayStack(this.order.length);
        byte values_pointer =0;
        byte operations_pointer = 0;

        for (boolean isNumber : this.order) {
            if (isNumber){
                stack.push(values[values_pointer++] );
            } else {
                if (stack.size() < 2) {
                    throw new IllegalStateException("Invalid expression: " + this.toString());
                }
                double b = stack.pop();
                double a = stack.pop();
                
                double result= Operation.getOperations()[operations[operations_pointer++]].apply(a, b);
                if (Double.isNaN(result)) {
                    return result;
                }
                stack.push(result);
            }
        }
        double nonRounded = stack.pop();
        return Math.round(nonRounded * Math.pow(10, rounding)) / Math.pow(10,rounding);
    }

    public static String convertToParenthetical(Expression expression) {
        /*
        Converts the expression to a parenthetical string.
        expression: the expression to convert.
        */
        Stack<String> stack = new Stack<>();
        byte values_pointer =0;
        byte operations_pointer = 0;

        for (boolean isNumber : expression.order) {
            if (isNumber) {
                stack.push(String.valueOf(expression.valueOrder[values_pointer++]));
            } else {
                byte opCode = expression.operations[operations_pointer++];
                String b = stack.pop();
                String a = stack.pop();
                String combinedExpression = "("+a+String.valueOf(Operation.getOperations()[opCode])+b+")";
                stack.push(combinedExpression);
            
            }   
        }
        return stack.pop();
    }

    public static Expression createExpressionFromString(String expression) {
        /*
        Creates an expression from a parenthetical string.
        expression: the parenthetical string to convert.
        */
        //TODO: implement this method
        return null;
    }
    //combined a helper function in order to make the code run more efficiently
    public static Expression[] createCombinedExpressions(Expression expression1, Expression expression2) {
        /*
        Creates all possible expressions made by combining two expressions.
        expression1: the first expression to combine.
        expression2: the second expression to combine.

        Avoids returning expressions that are the same up to commutativity.
        */
        int index=0;
        Expression[] toReturn = new Expression[Operation.getNumOperationOrderings()];
        for (byte opCode=0;opCode<Operation.getOperations().length;opCode++) {
            if (!Operation.getOperations()[opCode].isCommutative()) {
                toReturn[index++]=combineExpressions(expression1, expression2, opCode);
                toReturn[index++]=combineExpressions(expression2, expression1, opCode);
            } else {
                toReturn[index++]=combineExpressions(expression1, expression2, opCode);
            }
        }
        return toReturn;
    }



    private static Expression combineExpressions(Expression expr1, Expression expr2, byte opCode) {
        /*
        Combines two expressions.
        expr1: the first expression to combine.
        expr2: the second expression to combine.
        opCode: the operation to combine the expressions.
        */
        byte[] newValueOrder = combine(expr1.valueOrder,expr2.valueOrder);
        byte[] newOperations = combineWithExtraSpot(expr1.operations,expr2.operations);
        newOperations[newOperations.length-1]=opCode;
        boolean[] newOrder =combineWithExtraSpot(expr1.order,expr2.order);
        newOrder[newOrder.length-1]=false;

        return new Expression(newValueOrder, newOperations, newOrder);

    }
    public static byte[] combine(byte[] arr1, byte[] arr2) {
        /*
        Combines two byte arrays.
        arr1: the first byte array to combine.
        arr2: the second byte array to combine.
        */
        byte[] newArr = new byte[arr1.length + arr2.length];
        

        System.arraycopy(arr1, 0, newArr, 0, arr1.length);
        System.arraycopy(arr2, 0, newArr, arr1.length, arr2.length);
        
        return newArr;
    }

    // Overloaded method for combining byte arrays
    public static byte[] combineWithExtraSpot(byte[] arr1, byte[] arr2) {
        /*
        Combines two byte arrays with an extra spot.
        arr1: the first byte array to combine.
        arr2: the second byte array to combine.

        Used in combining expressions.
        */
        byte[] newArr = new byte[arr1.length + arr2.length+1];
        
        System.arraycopy(arr1, 0, newArr, 0, arr1.length);
        System.arraycopy(arr2, 0, newArr, arr1.length, arr2.length);
        
        return newArr;
    }
    public static boolean[] combineWithExtraSpot(boolean[] arr1, boolean[] arr2) {
        /*
        Combines two boolean arrays with an extra spot.
        arr1: the first boolean array to combine.
        arr2: the second boolean array to combine.

        Used in combining expressions.
        */
        boolean[] newArr = new boolean[arr1.length + arr2.length+1];
        System.arraycopy(arr1, 0, newArr, 0, arr1.length);
        System.arraycopy(arr2, 0, newArr, arr1.length, arr2.length);
        return newArr;
    }

}

