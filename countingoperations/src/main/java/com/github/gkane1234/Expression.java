package com.github.gkane1234;
import java.util.*;

public class Expression {
    private static final int ROUNDING=7;
    public byte[] values;
    public byte[] operations;
    public boolean[] order;
    public Expression(byte[] values,byte[] operations,boolean[] order) {

        this.values=values;
        this.operations=operations;
        this.order=order;
    }

    public Expression(Expression expression) {
        this(expression.values,expression.operations,expression.order);
    }


    public Expression change_values(byte[] values) {
        byte[] newValues = new byte[values.length];
        for (byte i=0;i<values.length;i++) {
            newValues[i]=values[this.values[i]];
        }

        return new Expression(newValues,this.operations,this.order);
    }

    public double evaluate_with_values(double[] values,int rounding) {
        double[] newValues = new double[values.length];
        for (byte i=0;i<values.length;i++) {
            newValues[i]=values[this.values[i]];
        }

        double val = this.evaluate(newValues,rounding);

        return val;


        
    }

    @Override
    public String toString() {
        return Expression.convertToParenthetical(this);
    
    }



    private double evaluate(double[] values,int rounding) {
        return evaluateRpn(values,rounding);
    }

    public double evaluateRpn(double[] values,int rounding) {
        DoubleArrayStack stack = new DoubleArrayStack(this.order.length);
        byte values_pointer =0;
        byte operations_pointer = 0;

        for (boolean isNumber : this.order) {
            if (isNumber){
                stack.push(values[values_pointer++]);
            } else {
                if (stack.size() < 2) {
                    throw new IllegalStateException("Invalid expression: " + this.toString());
                }
                double b = stack.pop();
                double a = stack.pop();
                
                double result= Operation.OPERATIONS[operations[operations_pointer++]].apply(a, b);
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
        
        Stack<String> stack = new Stack<>();
        byte values_pointer =0;
        byte operations_pointer = 0;

        for (boolean isNumber : expression.order) {
            if (isNumber) {
                stack.push(String.valueOf(expression.values[values_pointer++]));
            } else {
                byte opCode = expression.operations[operations_pointer++];
                String b = stack.pop();
                String a = stack.pop();
                String combinedExpression = "("+a+String.valueOf(Operation.OPERATIONS[opCode])+b+")";
                stack.push(combinedExpression);
            
            }   
        }
        return stack.pop();
    }

    public static Expression createExpressionFromString(String expression) {
        //TODO: implement this method
        return null;
    }
    //combined a helper function in order to make the code run more efficiently
    public static List<Expression> createCombinedExpressions(Expression expression1, Expression expression2) {
        List<Expression> toReturn = new ArrayList<>();
        for (byte opCode=0;opCode<Operation.OPERATIONS.length;opCode++) {
            if (!Operation.OPERATIONS[opCode].isCommutative()) {
                toReturn.add(combineExpressions(expression1, expression2, opCode));
                toReturn.add(combineExpressions(expression2, expression1, opCode));
            } else {
                toReturn.add(combineExpressions(expression1, expression2, opCode));
            }
        }
        return toReturn;
    }



    private static Expression combineExpressions(Expression expr1, Expression expr2, byte opCode) {

        byte[] newValues = combine(expr1.values,expr2.values);
        byte[] new_operations = combine_with_extra_spot(expr1.operations,expr2.operations);
        new_operations[new_operations.length-1]=opCode;
        boolean[] new_order =combine_with_extra_spot(expr1.order,expr2.order);
        new_order[new_order.length-1]=false;

        return new Expression(newValues, new_operations, new_order);

    }
    public static byte[] combine(byte[] arr1, byte[] arr2) {
        byte[] newArr = new byte[arr1.length + arr2.length];
        
        // Use System.arraycopy for efficient copying
        System.arraycopy(arr1, 0, newArr, 0, arr1.length);
        System.arraycopy(arr2, 0, newArr, arr1.length, arr2.length);
        
        return newArr;
    }

    // Overloaded method for combining byte arrays
    public static byte[] combine_with_extra_spot(byte[] arr1, byte[] arr2) {
        byte[] newArr = new byte[arr1.length + arr2.length+1];
        
        // Use System.arraycopy for efficient copying
        System.arraycopy(arr1, 0, newArr, 0, arr1.length);
        System.arraycopy(arr2, 0, newArr, arr1.length, arr2.length);
        
        return newArr;
    }
    public static boolean[] combine_with_extra_spot(boolean[] arr1, boolean[] arr2) {
        boolean[] newArr = new boolean[arr1.length + arr2.length+1];
        System.arraycopy(arr1, 0, newArr, 0, arr1.length);
        System.arraycopy(arr2, 0, newArr, arr1.length, arr2.length);
        return newArr;
    }

}

