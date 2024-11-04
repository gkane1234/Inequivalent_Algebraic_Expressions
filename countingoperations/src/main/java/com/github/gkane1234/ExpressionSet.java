package com.github.gkane1234;
import java.util.Random;
import java.util.Arrays;
import gnu.trove.set.hash.TFloatHashSet;
public class ExpressionSet {
    /*
    A data structure that stores inequivalent expressions.

    To be added to the set, an expression is found if it is not equivalent to any of the expressions already in the set,
    by using a number of tester lists of values, each list being called a truncator.

    */
    private Expression[] expressions;
    private int numExpressions;
    private int numValues;
    public int rounding;

    private TFloatHashSet[] seen;
    //private TCustomHashSet<Float>[] seen;
    private double[][] truncators;
    public int numTruncators;
    public Operation[] ops;

    // Constructor
    public ExpressionSet(int numValues, int rounding, int numTruncators) {
        /*
        Constructor for an ExpressionSet.
        numValues: the number of values in the expressions.
        rounding: the number of decimal places to round to.
        numTruncators: the number of truncators to use.
        */
        this(new Expression[] {}, 0, numValues, rounding, numTruncators);
    }
    public ExpressionSet(Expression[] expressions,int numExpressions,int numValues, int rounding, int numTruncators) {
        /*
        Constructor for an ExpressionSet.
        expressions: expressions that have already been found to be inequivalent.
        numExpressions: the number of expressions that are in expressions. (This can differ from the length of expressions)
        numValues: the number of values in the expressions.
        rounding: the number of decimal places to round to.
        numTruncators: the number of truncators to use.
        */
        if (expressions.length==0) {
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
        for (int i = 0; i < numTruncators; i++) {
            TFloatHashSet set = new TFloatHashSet();
            seen[i]=set;
            //seen[i]=new TCustomHashSet<>(strategy);
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
    public Expression get(int i) {
        return this.expressions[i];
    }

    public int size() {
        return this.numExpressions;
    }


    public Expression[] getExpressions() {
        return expressions;
    }
    public int getNumExpressions() {
        return this.numExpressions;
    }
    


    @Override
    public String toString() {
        return Arrays.toString(expressions);
    }

    public ExpressionSet changeValueOrders(byte[] valueOrder) {
        return createEvaluatedExpressionSet(this, valueOrder, this.numTruncators);
    }

    public boolean add(Expression expression) {
        boolean toAdd = false;
        for (int i = 0; i < this.numTruncators; i++) {
            double value = expression.evaluateWithValues(this.truncators[i],this.rounding);
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
    public static ExpressionSet createEvaluatedExpressionSet(ExpressionSet genericExpressionSet, byte[] value_order, int numTruncators) throws IllegalStateException {
        
        Expression[] newExpressions = new Expression[genericExpressionSet.expressions.length];
        for (int i=0;i<genericExpressionSet.numExpressions;i++) {
            newExpressions[i]=genericExpressionSet.expressions[i].changeValueOrder(value_order);
            
        }
        return new ExpressionSet(newExpressions, genericExpressionSet.numExpressions,genericExpressionSet.numValues, genericExpressionSet.rounding, numTruncators);
    }

}