package com.github.gkane1234;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gnu.trove.set.hash.TFloatHashSet;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
/*
    A data structure that stores inequivalent expressions.

    To be added to the set, an expression is found if it is not equivalent to any of the expressions already in the set,
    by using a number of tester lists of values, each list being called a truncator.

*/
public class ExpressionSet implements Serializable{
    private static final long serialVersionUID = 1L;

    private static final String FILE_PATH = "counting_operations/outputs/";

    private static final int THREASHOLD = 2;
    
    private Expression[] expressions;
    private int numExpressions;
    private int numValues;
    private int rounding;
    private TFloatHashSet[] seen;
    //private CustomFloatHashSet[] seen;
    //private TCustomHashSet<Float>[] seen;
    private double[][] truncators;
    public int numTruncators;
    public Operation[] ops;

    /**
        Constructor for an ExpressionSet.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param rounding: an <code>int</code> representing the number of decimal places to round to.
        @param numTruncators: an <code>int</code> representing the number of truncators to use.
    */
    public ExpressionSet(int numValues, int rounding, int numTruncators) {
        
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
    public ExpressionSet(Expression[] expressions,int numExpressions,int numValues, int rounding, int numTruncators) {
        
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
    public ExpressionSet changeValueOrders(byte[] valueOrder) {
        return createEvaluatedExpressionSet(this, valueOrder, this.numTruncators);
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
    /**
        Adds an expression to the set without checking if it is equivalent to any of the expressions already in the set.
        @param expression: an <code>Expression</code> to add to the set.
    */
    public void forceAdd(Expression expression) {
        expressions[this.numExpressions++]=expression;
    }


    public static void save(ExpressionSet expressionSet) {
        expressionSet.clearSeen();
        String filename = FILE_PATH+"expressionSet"+expressionSet.numValues+"_"+expressionSet.numExpressions+".ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {

            oos.writeObject(expressionSet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void saveLarge(ExpressionSet expressionSet) {
        expressionSet.clearSeen();
        String filename = FILE_PATH + "expressionSetLarge" + expressionSet.numValues + "_" + expressionSet.numExpressions + ".ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            for (int i = 0; i < expressionSet.numExpressions; i++) {
                if (i%1000000==0) {
                    System.out.println("Saving expression "+i);
                }
                oos.writeObject(expressionSet.expressions[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static ExpressionSet load(String filename) throws FileNotFoundException {
        System.out.println("Loading from "+filename);
        ExpressionSet expressionSet = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            expressionSet = (ExpressionSet) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileNotFoundException("File not found: "+filename);
        }
        return expressionSet;   
    }

    public static ExpressionSet load(int numValues) throws FileNotFoundException {
        int numExpressions = getMaximumSize(numValues);
        String filename = FILE_PATH+"expressionSet"+numValues+"_"+numExpressions+".ser";
        return load(filename);
    }
    public static ExpressionSet load(int numValues, int numExpressions) throws FileNotFoundException {
        String filename = FILE_PATH+"expressionSet"+numValues+"_"+numExpressions+".ser";
        return load(filename);

    }

    public static ExpressionSet loadLarge(int numValues, int numExpressions, int numThreads) throws FileNotFoundException {
        String filename = FILE_PATH+"expressionSetLarge"+numValues+"_"+numExpressions+".ser";
        return loadLarge(filename, numValues, numExpressions, numThreads);
    }

    public static ExpressionSet loadLarge(String filename, int numValues,int numExpressions, int numThreads) throws FileNotFoundException {
        
        // Use a thread-safe list to store expressions
        ConcurrentLinkedQueue<Expression> expressionsQueue = new ConcurrentLinkedQueue<>();
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            List<Future<Void>> futures = new ArrayList<>();
            
            // Read the total number of expressions first
            for (int i = 0; i < numExpressions; i++) {
                futures.add(executor.submit(() -> {
                    try {
                        Expression expression;
                        synchronized (ois) {
                            expression = (Expression) ois.readObject();
                        }
                        
                        expressionsQueue.add(expression);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        
                    }
                    return null; // Return type must match Future's type
                }));
            }
            
            // Wait for all tasks to complete
            for (Future<Void> future : futures) {
                try {
                    future.get(); // This will block until the task is complete
                } catch (ExecutionException e) {
                    e.printStackTrace(); // Handle exceptions from the task
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new FileNotFoundException("File not found: " + filename);
        } finally {
            executor.shutdown(); // Shutdown the executor
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Transfer expressions from the queue to the expressionSet array
        int index = 0;
        Expression[] expressions = new Expression[numExpressions];
        for (Expression expr : expressionsQueue) {
            expressions[index++] = expr;
        }

        return new ExpressionSet(expressions, numExpressions, numValues, 0, 0);
    }



    /**
        Creates a new ExpressionSet with a new value order.
        @param genericExpressionSet: an <code>ExpressionSet</code> to create a new set from.
        @param value_order: a <code>byte[]</code> representing the new value order.
        @param numTruncators: the number of truncators to use.
        @return an <code>ExpressionSet</code> representing the new set.
    */
    public static ExpressionSet createEvaluatedExpressionSet(ExpressionSet genericExpressionSet, byte[] value_order, int numTruncators) throws IllegalStateException {
        
        Expression[] newExpressions = new Expression[genericExpressionSet.expressions.length];
        for (int i=0;i<genericExpressionSet.numExpressions;i++) {
            newExpressions[i]=genericExpressionSet.expressions[i].changeValueOrder(value_order);
            
        }
        return new ExpressionSet(newExpressions, genericExpressionSet.numExpressions,genericExpressionSet.numValues, genericExpressionSet.rounding, numTruncators);
    }

}