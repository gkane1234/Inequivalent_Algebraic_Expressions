package com.github.gkane1234;
import java.util.Random;

import gnu.trove.set.hash.TFloatHashSet;

public class ExpressionList {
    private Expression[] expressions;
    private int numExpressions;
    private int numValues;
    public int rounding;
    public boolean genericExpressions;

    private TFloatHashSet[] seen;
    private double[][] truncators;
    public int numTruncators;
    public Operation[] ops;

    // Constructor
    public ExpressionList(int numValues, int rounding, int numTruncators,boolean genericExpressions) {
        this(new Expression[] {}, 0, numValues, rounding, numTruncators, genericExpressions);
    }
    public ExpressionList(Expression[] expressions,int numExpressions,int numValues, int rounding, int numTruncators,boolean genericExpressions) {
        if (expressions.length==0) {
            this.expressions=new Expression[getMaximumSize(numValues)];
            this.numExpressions=0;
        } else {
            this.expressions=expressions;
            this.numExpressions=numExpressions;
        }
        this.numValues = numValues;
        this.rounding = rounding;
        this.genericExpressions = genericExpressions;

        this.seen = new TFloatHashSet[numTruncators];
        this.truncators = new double[numTruncators][numValues];
        this.numTruncators = numTruncators;
        
        Random random = new Random();
        for (int i = 0; i < numTruncators; i++) {
            TFloatHashSet set = new TFloatHashSet();
            seen[i]=set;
            double[] truncator = new double[numValues];
            for (int j = 0; j < numValues; j++) {
                truncator[j] = random.nextDouble();
            }
            truncators[i]=truncator;
        }

    }
    public static int getMaximumSize(int numValues) {
        return Counter.run(numValues).intValue();
    }
    public int getNumValues() {
        return this.numValues;
    }
    // Getter for a specific expression (equivalent to __getitem__)
    public Expression get(int i) {
        return this.expressions[i];
    }

    // Length of the expression list (equivalent to __len__)
    public int size() {
        return this.numExpressions;
    }

    // Iterating over expressions (equivalent to __iter__)
    public Expression[] getExpressions() {
        return expressions;
    }
    public int getNumExpressions() {
        return this.numExpressions;
    }
    

    // String representation (equivalent to __str__ and __repr__)
    @Override
    public String toString() {
        return expressions.toString();
    }

    public ExpressionList evaluate(byte[] values) {
        return createEvaluatedExpressionList(this, values, this.numTruncators);
    }

    // Add an expression to the list
    public boolean add(Expression expression) {
        boolean toAdd = false;
        for (int i = 0; i < this.numTruncators; i++) {
            double value = expression.evaluate_with_values(this.truncators[i],this.rounding);
            if (!Double.isNaN(value) && seen[i].add((float)value)) {
                toAdd = true;  
            }
        }
        if (toAdd) {
            expressions[this.numExpressions++]=expression;
        }
        return toAdd;
    }



    // Static method to create a new expression list with the given values
    public static ExpressionList createEvaluatedExpressionList(ExpressionList genericExpressionList, byte[] values, int numTruncators) throws IllegalStateException {
        
        if (!genericExpressionList.genericExpressions) {
            throw new IllegalStateException("Input must be a generic expression list");
        }
        Expression[] newExpressions = new Expression[genericExpressionList.expressions.length];
        for (int i=0;i<genericExpressionList.numExpressions;i++) {
            newExpressions[i]=genericExpressionList.expressions[i].change_values(values);
            
        }
        return new ExpressionList(newExpressions, genericExpressionList.numExpressions,genericExpressionList.numValues, genericExpressionList.rounding, numTruncators,true);
    }

}