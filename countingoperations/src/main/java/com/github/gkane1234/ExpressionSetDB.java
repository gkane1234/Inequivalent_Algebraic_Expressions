package com.github.gkane1234;
import java.util.Random;
import java.util.Arrays;
import org.rocksdb.RocksDBException;






/*
    A data structure that stores inequivalent expressions.

    To be added to the set, an expression is found if it is not equivalent to any of the expressions already in the set,
    by using a number of tester lists of values, each list being called a truncator.

*/
public class ExpressionSetDB extends CompressedExpressionSet{
    private RocksFloatHashSet[] seen;
    //private CustomFloatHashSet[] seen;
    //private TCustomHashSet<Float>[] seen;

    /**
        Constructor for an ExpressionSet.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param rounding: an <code>int</code> representing the number of decimal places to round to.
        @param numTruncators: an <code>int</code> representing the number of truncators to use.
    */


    public ExpressionSetDB(int numValues, int rounding, int numTruncators) {
        this(new long[ExpressionCompression.getCompressedExpressionSetSize(getMaximumSize(numValues),ExpressionCompression.REQUIRED_BITS(numValues)[3])],0,numValues,rounding,numTruncators);
    }
    /**
        Constructor for an ExpressionSet.
        @param expressions: expressions that have already been found to be inequivalent.
        @param numExpressions: an <code>int</code> representing the number of expressions that are in expressions. (This can differ from the length of expressions)
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param rounding: an <code>int</code> representing the number of decimal places to round to.
        @param numTruncators: an <code>int</code> representing the number of truncators to use.
    */
    
    public ExpressionSetDB(long[] compressedExpressions, int numExpressions, int numValues, int rounding, int numTruncators) {
        super(compressedExpressions, numExpressions, numValues);
        
        this.rounding = rounding;

        this.seen = new RocksFloatHashSet[numTruncators];

        this.truncators = new double[numTruncators][numValues];
        this.numTruncators = numTruncators;
        //System.err.println(ExpressionCompression.getCompressedExpressionSetSize(getMaximumSize(numValues),ExpressionCompression.REQUIRED_BITS(numValues)[3]));
        
        //System.err.println(compressedExpressions.length);
        
        Random random = new Random();


        //System.err.println("Creating "+numTruncators+" truncators");

        double maxTruncatorValue = 10;
        for (int i = 0; i < numTruncators; i++) {
            try {
                seen[i]=new RocksFloatHashSet(FILE_PATH+"/db/seen_"+i+"_"+System.currentTimeMillis()+".db");

                //seen[i]=new TCustomHashSet<>(strategy);
                double[] truncator = new double[numValues];
                for (int j = 0; j < numValues; j++) {
                    truncator[j] = 2*random.nextDouble()*maxTruncatorValue-maxTruncatorValue; // have answers be well mixed in the range of all possible floats to aid in hashing
                    }
                    truncators[i]=truncator;
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        Returns the maximum number of expressions that can be in the set.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @return an <code>int</code> representing the maximum number of expressions that can be in the set.
    */
    public static int getMaximumSize(int numValues) {
        return Counter.run(numValues).intValue();
    }
    /**
        Returns the number of values in the expressions.
        @return an <code>int</code> representing the number of values in the expressions.
    */
    public int getNumValues() {
        return this.numValues;
    }

    /**
        Returns the number of expressions in the set.
        @return an <code>int</code> representing the number of expressions in the set.
    */
    public int size() {
        return this.numExpressions;
    }
    /**
        Returns all the expressions in the set.
        @return an <code>Expression[]</code> representing all the expressions in the set.
    */
    public Expression[] getExpressions() {
        return ExpressionCompression.decompressExpressionSet(compressedExpressions, numExpressions, numValues, false).getExpressions();
    }
    public int getNumExpressions() {
        return this.numExpressions;
    }
    

    /**
        Returns a string representation of the set.
        @return a <code>String</code> representing the set.
    */
    @Override
    public String toString() {
        return Arrays.toString(getExpressions());
    }
    @Override
    public void cleanup() {
        for (int i = 0; i < this.numTruncators; i++) {
            this.seen[i].delete();
        }
        super.cleanup();
        System.err.println("Cleaning up");
        
    }
    /**
        Creates a new ExpressionSet with a new value order.
        @param value_order: a <code>byte[]</code> representing the new value order.

    */
    @Override
    public ExpressionSet changeValueOrder(byte[] valueOrder) throws IllegalStateException {
        throw new IllegalStateException("A compressed expression cannot be changed to a new value order");
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
            ExpressionCompression.setCompressedExpression(compressedExpressions, numExpressions, expressionSize, ExpressionCompression.compressExpression(expression));
            numExpressions++;
        }
        return toAdd;
    }
    /**
        Adds an expression to the set without checking if it is equivalent to any of the expressions already in the set.
        @param expression: an <code>Expression</code> to add to the set.
    */
    public void forceAdd(Expression expression) {
        ExpressionCompression.setCompressedExpression(compressedExpressions, numExpressions, expressionSize, ExpressionCompression.compressExpression(expression));
        numExpressions++;
    }

    @Override
    public boolean equals(Object o) {
        //System.err.println(this);
        //System.err.println(o);
        if (o instanceof ExpressionSetDB) {
            ExpressionSetDB e = (ExpressionSetDB)o;
            if (this.numExpressions!=e.numExpressions) {
                return false;
            }
            for (int i = 0; i < this.numExpressions; i++) {
                //System.out.println(this.expressions[i]+" "+e.expressions[i]);
                if (!this.get(i).equals(e.get(i))) {
                    return false;
                }

            }
            return true;
        }
        return false;
    }




    /**
        Creates a new ExpressionSet with a new value order.
        @param expressionSet: an <code>ExpressionSet</code> to create a new set from.
        @param value_order: a <code>byte[]</code> representing the new value order.
        @param numTruncators: the number of truncators to use.
        @return an <code>ExpressionSet</code> representing the new set.
    */
    public static ExpressionSetDB changeValueOrder(ExpressionSetDB expressionSet, byte[] value_order, int numTruncators) throws IllegalStateException {
        
        Expression[] newExpressions = new Expression[expressionSet.getNumExpressions()];
        for (int i=0;i<expressionSet.getNumExpressions();i++) {
            newExpressions[i]=expressionSet.get(i).changeValueOrder(value_order);
        }

        long[] compressedExpressions = ExpressionCompression.compressExpressionSet(new ExpressionSet(newExpressions, newExpressions.length, expressionSet.getNumValues()));


        return new ExpressionSetDB(compressedExpressions, newExpressions.length,expressionSet.getNumValues(), expressionSet.rounding, numTruncators);
    }
}
