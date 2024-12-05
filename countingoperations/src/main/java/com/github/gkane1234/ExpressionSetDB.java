package com.github.gkane1234;
import java.util.Random;





/*
    A data structure that stores inequivalent expressions.

    To be added to the set, an expression is found if it is not equivalent to any of the expressions already in the set,
    by using a number of tester lists of values, each list being called a truncator.

    This class uses a RocksDB database to store the seen values for each truncator.

    This class only deals with compressed expressions.

*/
public class ExpressionSetDB extends CompressedExpressionList{
    private RocksFloatHashSet[] seen;
    private int rounding;
    private int numTruncators;
    private double[][] truncators;

    /**
        Constructor for an ExpressionSetDB.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param rounding: an <code>int</code> representing the number of decimal places to round to.
        @param numTruncators: an <code>int</code> representing the number of truncators to use.
    */
    public ExpressionSetDB(int numValues, int rounding, int numTruncators) {
        this(new long[ExpressionCompression.getCompressedExpressionListSize(getMaximumSize(numValues),ExpressionCompression.REQUIRED_BITS(numValues)[3])],0,numValues,rounding,numTruncators);
    }
    /**
        Constructor for an ExpressionSetDB.
        @param compressedExpressions: the compressed expressions to use.
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
                    truncator[j] = 2*random.nextDouble()*maxTruncatorValue-maxTruncatorValue;
                }
                truncators[i]=truncator;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    /**
        Constructor for an ExpressionSetDB.
        @param compressedExpressionList: the compressed expressions to use.
        @param numTruncators: an <code>int</code> representing the number of truncators to use.
    */
    public ExpressionSetDB(CompressedExpressionList compressedExpressionList, int numTruncators) {
        super(compressedExpressionList);
        this.numTruncators = numTruncators;
        this.seen = new RocksFloatHashSet[numTruncators];
        this.truncators = new double[numTruncators][numValues];
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
    public ExpressionList changeValueOrder(byte[] valueOrder) throws IllegalStateException {
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
}
