package com.github.gkane1234;
import java.util.Random;
import java.util.Arrays;
import gnu.trove.set.hash.TFloatHashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
/*
    A data structure that stores inequivalent expressions.

    To be added to the set, an expression is found if it is not equivalent to any of the expressions already in the set,
    by using a number of tester lists of values, each list being called a truncator.

*/
public class ExpressionSetThreaded {
    
    private Expression[] expressions;
    private int numExpressions;
    private int numValues;
    public int rounding;
    private Thread[] truncatorThreads;
    private BlockingQueue<Expression>[] truncatorQueues;
    private BlockingQueue<Boolean>[] resultQueues;
    private double[][] truncators;
    public int numTruncators;
    public Operation[] ops;
    private AtomicBoolean isRunning;

    /**
        Constructor for an ExpressionSet.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param rounding: an <code>int</code> representing the number of decimal places to round to.
        @param numTruncators: an <code>int</code> representing the number of truncators to use.
    */
    public ExpressionSetThreaded(int numValues, int rounding, int numTruncators) {
        this(new Expression[] {}, 0, numValues, rounding, numTruncators);
    }
    /**
        Constructor for an ExpressionSet.
        @param expressions: expressions that have already been found to be inequivalent.
        @param numExpressions: an <code>int</code> representing the number of expressions that are in expressions. (This can differ from the length of expressions)
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param rounding: an <code>int</code> representing the number of decimal places to round to.
        @param numTruncators: an <code>int</code> representing the number of truncators to use.
    */
    @SuppressWarnings("unchecked")
    public ExpressionSetThreaded(Expression[] expressions,int numExpressions,int numValues, int rounding, int numTruncators) {
        
        if (expressions.length==0) {
            this.expressions=new Expression[getMaximumSize(numValues)];
            this.numExpressions=0;
        } else {
            this.expressions=expressions;
            this.numExpressions=numExpressions;
        }
        this.numValues = numValues;
        this.rounding = rounding;

        this.truncatorThreads = new Thread[numTruncators];
        this.truncatorQueues = new BlockingQueue[numTruncators];
        this.resultQueues = new BlockingQueue[numTruncators];
        this.truncators = new double[numTruncators][numValues];
        this.numTruncators = numTruncators;
        this.isRunning = new AtomicBoolean(false);
        
        Random random = new Random();

        double maxTruncatorValue = 10;
        for (int i = 0; i < numTruncators; i++) {
            truncatorQueues[i] = new LinkedBlockingQueue<>();
            resultQueues[i] = new LinkedBlockingQueue<>();
            double[] truncator = new double[numValues];
            for (int j = 0; j < numValues; j++) {
                truncator[j] = 2*random.nextDouble()*maxTruncatorValue-maxTruncatorValue; // have answers be well mixed in the range of all possible floats to aid in hashing
            }
            truncators[i]=truncator;
        }
    }

    public void startAdding() {
        System.out.println("Starting adding");
        isRunning.set(true);
        for (int i = 0; i < numTruncators; i++) {
            final int truncatorIndex = i;
            truncatorThreads[i] = new Thread(() -> {
                TFloatHashSet seen = new TFloatHashSet();
                while (isRunning.get() || !truncatorQueues[truncatorIndex].isEmpty()) {
                    try {
                        //System.out.println("Thread "+truncatorIndex+" waiting for expression");
                        Expression expr = truncatorQueues[truncatorIndex].take();
                        //System.out.println("Thread "+truncatorIndex+" got expression "+expr);
                        double value = expr.evaluateWithValues(truncators[truncatorIndex], rounding);
                        boolean result = false;
                        if (!Double.isNaN(value)) {
                            result = seen.add((float)value);
                        }
                        resultQueues[truncatorIndex].put(result);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            truncatorThreads[i].start();
        }
    }

    public void doneAdding() {
        isRunning.set(false);
        for (Thread thread : truncatorThreads) {
            try {
               
                thread.interrupt();
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
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
        Returns the expression at a given index.
        @param i: an <code>int</code> representing the index of the expression to return.
        @return an <code>Expression</code> representing the expression at the given index.
    */
    public Expression get(int i) {
        return this.expressions[i];
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
        return expressions;
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
        return Arrays.toString(expressions);
    }
    /**
        Changes the value order of the expressions in the set.
        @param valueOrder: a <code>byte[]</code> representing the new value order.
        @return an <code>ExpressionSet</code> representing the set with the new value order.
    */
    public ExpressionSetThreaded changeValueOrders(byte[] valueOrder) {
        return createEvaluatedExpressionSet(this, valueOrder, this.numTruncators);
    }
    /**
        Adds an expression to the set if it is not equivalent to any of the expressions already in the set.
        @param expression: an <code>Expression</code> to add to the set.
        @return a <code>boolean</code> representing whether the expression was added to the set.
    */
    public boolean add(Expression expression) {
        // Submit expression to all truncator threads
        for (int i = 0; i < numTruncators; i++) {
            try {
                truncatorQueues[i].put(expression);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        // Collect results from all threads
        boolean toAdd = false;
        for (int i = 0; i < numTruncators; i++) {
            try {
                if (resultQueues[i].take()) {
                    toAdd = true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        if (toAdd) {
            expressions[this.numExpressions++] = expression;
        }
        return toAdd;
    }
    /**
        Adds an expression to the set without checking if it is equivalent to any of the expressions already in the set.
        @param expression: an <code>Expression</code> to add to the set.
    */
    public void forceAdd(Expression expression) {
        expressions[this.numExpressions++] = expression;
    }

    /**
        Creates a new ExpressionSet with a new value order.
        @param genericExpressionSet: an <code>ExpressionSet</code> to create a new set from.
        @param value_order: a <code>byte[]</code> representing the new value order.
        @param numTruncators: the number of truncators to use.
        @return an <code>ExpressionSet</code> representing the new set.
    */
    public static ExpressionSetThreaded createEvaluatedExpressionSet(ExpressionSetThreaded genericExpressionSet, byte[] value_order, int numTruncators) throws IllegalStateException {
        
        Expression[] newExpressions = new Expression[genericExpressionSet.expressions.length];
        for (int i=0;i<genericExpressionSet.numExpressions;i++) {
            newExpressions[i]=genericExpressionSet.expressions[i].changeValueOrder(value_order);
            
        }
        return new ExpressionSetThreaded(newExpressions, genericExpressionSet.numExpressions,genericExpressionSet.numValues, genericExpressionSet.rounding, numTruncators);
    }

}