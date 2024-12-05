package com.github.gkane1234;
import java.util.Random;

import gnu.trove.set.hash.TFloatHashSet;


/*
    A data structure that stores inequivalent expressions.

    To be added to the set, an expression is found if it is not equivalent to any of the expressions already in the set,
    by using a number of tester lists of values, each list being called a truncator.

*/
public class ExpressionSet extends ExpressionList{
    private static final long serialVersionUID = 1L;

    protected int rounding;
    private TFloatHashSet[] seen;
    protected double[][] truncators;
    protected int numTruncators;

    /**
        Constructor for an ExpressionSet.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param rounding: an <code>int</code> representing the number of decimal places to round to.
        @param numTruncators: an <code>int</code> representing the number of truncators to use.
    */
    public ExpressionSet(int numValues, int rounding, int numTruncators) {
        this(new Expression[] {}, 0, numValues, rounding, numTruncators);
    }

    public ExpressionSet(Expression[] expressions, int numExpressions, int numValues) {
        this(expressions, numExpressions, numValues, 0, 0);
    }
    /**
        Constructor for an ExpressionSet.
        @param expressions: expressions that have already been found to be inequivalent.
        @param numExpressions: an <code>int</code> representing the number of expressions that are in expressions. (This can differ from the length of expressions)
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param rounding: an <code>int</code> representing the number of decimal places to round to.
        @param numTruncators: an <code>int</code> representing the number of truncators to use.
    */
    
    public ExpressionSet(Expression[] expressions,int numExpressions,int numValues, int rounding, int numTruncators) {

        if (expressions!=null&&expressions.length==0) {
            this.expressions=new Expression[getMaximumSize(numValues)];
            this.numExpressions=0;
        } else {
            this.expressions=expressions;
            this.numExpressions=numExpressions;
        }
        
        this.numValues = numValues;
        this.rounding = rounding;

        this.seen = new TFloatHashSet[numTruncators];
        //this.seen = new TCustomHashSet[numTruncators];
        //FloatHashingStrategy strategy = new FloatHashingStrategy();

        this.truncators = new double[numTruncators][numValues];
        this.numTruncators = numTruncators;
        
        Random random = new Random();

        double maxTruncatorValue = 10;
        for (int i = 0; i < numTruncators; i++) {
            TFloatHashSet set = new TFloatHashSet();
            seen[i]=set;
            //seen[i]=new TCustomHashSet<>(strategy);
            double[] truncator = new double[numValues];
            for (int j = 0; j < numValues; j++) {
                truncator[j] = 2*random.nextDouble()*maxTruncatorValue-maxTruncatorValue; // have answers be well mixed in the range of all possible floats to aid in hashing
            }
            truncators[i]=truncator;
        }

    }

    /**
        Clears the seen hashset to free up memory.
    */
    public void clearSeen() {
        this.seen=null;
        this.truncators=null;
        this.numTruncators=0;

    }
    

    /**
        Adds an expression to the set if it is not equivalent to any of the expressions already in the set.
        @param expression: an <code>Expression</code> to add to the set.
        @return a <code>boolean</code> representing whether the expression was added to the set.
    */
    public boolean add(Expression expression) {
        int uniqueTruncators = 0;
        for (int i = 0; i < this.numTruncators; i++) {
            double value = expression.evaluateWithValues(this.truncators[i],this.rounding);
            if (!Double.isNaN(value) && seen[i].add((float)value)) {
                uniqueTruncators++;  
            }
        }
        boolean toAdd = uniqueTruncators>=THREASHOLD; //if the expression is unique for at least THREASHOLD truncators, add it, this is to prevent false positives
        if (toAdd) {
            expressions[this.numExpressions++]=expression;
        }
        return toAdd;
    }


    @Override
    public void cleanup() {
        this.clearSeen();
    }

}
