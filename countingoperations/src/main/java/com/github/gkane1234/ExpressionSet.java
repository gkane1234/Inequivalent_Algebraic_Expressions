package com.github.gkane1234;
import java.util.Random;
import java.util.Arrays;

import gnu.trove.set.hash.TFloatHashSet;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

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
        return changeValueOrder(this, valueOrder, this.numTruncators);
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

    /**
        Saves an expression set to a file.
        @param expressionSet: an <code>ExpressionSet</code> to save.
        @param verbose: a <code>boolean</code> representing whether to print progress.
    */
    public static void save(ExpressionSet expressionSet, boolean verbose) {
        final int BUFFER_SIZE = 1024*1024;
        expressionSet.clearSeen();
        String filename = FILE_PATH + "expressionSetLarge" + expressionSet.numValues + "_" + expressionSet.numExpressions + ".ser";
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename),BUFFER_SIZE);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            for (int i = 0; i < expressionSet.numExpressions; i++) {
                if (verbose&&i%1000000==0) {
                    System.out.println("Saving expression "+i);
                }
                oos.writeObject(expressionSet.expressions[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
        Loads the expression set from a file which contains all algebraically inequivalent expressions with a given number of values.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @return an <code>ExpressionSet</code> representing the loaded set.
    */
    public static ExpressionSet load(int numValues) throws FileNotFoundException {
        int numExpressions = getMaximumSize(numValues);
        String filename = FILE_PATH+"expressionSet"+numValues+"_"+numExpressions+".ser";
        return load(filename, numValues, numExpressions, true);
    }
    /**
        Loads the expression set from a file which contains numExpressions inequivalent expressions with a given number of values.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param numExpressions: an <code>int</code> representing the number of expressions in the set.
        @return an <code>ExpressionSet</code> representing the loaded set.
    */
    public static ExpressionSet load(int numValues, int numExpressions) throws FileNotFoundException {
        String filename = FILE_PATH+"expressionSet"+numValues+"_"+numExpressions+".ser";
        return load(filename, numValues, numExpressions, true);

    }
    /**
        Loads the expression set from a file which contains numExpressions inequivalent expressions with a given number of values.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param numExpressions: an <code>int</code> representing the number of expressions in the set.
        @param verbose: a <code>boolean</code> representing whether to print progress.
        @return an <code>ExpressionSet</code> representing the loaded set.
    */
    public static ExpressionSet load(int numValues, int numExpressions, boolean verbose) throws FileNotFoundException {
        String filename = FILE_PATH+"expressionSetLarge"+numValues+"_"+numExpressions+".ser";
        return load(filename, numValues, numExpressions, verbose);
    }
    /**
        Loads the expression set from a file which contains numExpressions inequivalent expressions with a given number of values.
        @param filename: a <code>String</code> representing the path to the file containing the expressions.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param numExpressions: an <code>int</code> representing the number of expressions in the set.
        @param verbose: a <code>boolean</code> representing whether to print progress.
        @return an <code>ExpressionSet</code> representing the loaded set.
    */
    public static ExpressionSet load(String filename, int numValues,int numExpressions, boolean verbose) throws FileNotFoundException {
        final int BUFFER_SIZE = 1024*1024;
        // Use a thread-safe list to store expressions
        long startTime = System.currentTimeMillis();

        int decile = numExpressions/10;
        Expression[] expressions = new Expression[numExpressions];
        
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename),BUFFER_SIZE);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            
            // Read expressions sequentially
            for (int i = 0; i < numExpressions; i++) {
                try {
                    Expression expression = (Expression) ois.readObject();
                    expressions[i]=expression;
                    
                    if (verbose && i % decile == 0) {
                        System.out.println("%"+String.format("%.0f",100.0*i/numExpressions)+" Loaded expression " + i + " of "+numExpressions);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            
        } catch (IOException e) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Loaded "+numExpressions+" expressions in "+((endTime-startTime)/1000.0)+" seconds");
        return new ExpressionSet(expressions, numExpressions, numValues, 0, 0);
    }



    /**
        Creates a new ExpressionSet with a new value order.
        @param expressionSet: an <code>ExpressionSet</code> to create a new set from.
        @param value_order: a <code>byte[]</code> representing the new value order.
        @param numTruncators: the number of truncators to use.
        @return an <code>ExpressionSet</code> representing the new set.
    */
    public static ExpressionSet changeValueOrder(ExpressionSet expressionSet, byte[] value_order, int numTruncators) throws IllegalStateException {
        
        Expression[] newExpressions = new Expression[expressionSet.expressions.length];
        for (int i=0;i<expressionSet.numExpressions;i++) {
            newExpressions[i]=expressionSet.expressions[i].changeValueOrder(value_order);
            
        }
        return new ExpressionSet(newExpressions, expressionSet.numExpressions,expressionSet.numValues, expressionSet.rounding, numTruncators);
    }
    public static EvaluatedExpressionList evaluate(ExpressionSet expressionSet, double[] values, int rounding) {
        final int numThreads = 10;
        EvaluatedExpression[] evaluatedExpressions = new EvaluatedExpression[expressionSet.expressions.length];
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Void>> futures = new ArrayList<>();

        int chunkSize = (expressionSet.numExpressions + numThreads - 1) / numThreads;
        for (int t = 0; t < numThreads; t++) {
            final int start = t * chunkSize;
            final int end = Math.min(start + chunkSize, expressionSet.numExpressions);
            
            futures.add(executor.submit(() -> {
                for (int i = start; i < end; i++) {
                    double value = expressionSet.expressions[i].evaluateWithValues(values, rounding);
                    evaluatedExpressions[i] = new EvaluatedExpression(expressionSet.expressions[i], values, value);
                }
                return null;
            }));
        }

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Error evaluating expressions", e);
            }
        }

        executor.shutdown();
        return new EvaluatedExpressionList(evaluatedExpressions);
    }

    public static SolutionList findSolutions(ExpressionSet expressionSet, double[] values, double goal, int rounding, int maxSolutions) {
        final int numThreads = 10;
        List<EvaluatedExpression> evaluatedExpressions = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Void>> futures = new ArrayList<>();
        
        final Object lock = new Object(); // For thread-safe list access
        AtomicInteger solutionsFound = new AtomicInteger(0);

        int chunkSize = (expressionSet.numExpressions - 1) / numThreads+1;
        for (int t = 0; t < numThreads; t++) {
            final int start = t * chunkSize;
            final int end = Math.min(start + chunkSize, expressionSet.numExpressions);
            
            futures.add(executor.submit(() -> {
                for (int i = start; i < end && solutionsFound.get() < maxSolutions; i++) {
                    
                    double value = expressionSet.expressions[i].evaluateWithValues(values, rounding);
                    if (Solver.equal(value, goal)) { // Check if value equals goal
                        System.out.println(solutionsFound.get()+" "+evaluatedExpressions.size());
                        
                        synchronized(lock) {
                            if (solutionsFound.get() < maxSolutions) {
                                evaluatedExpressions.add(new EvaluatedExpression(expressionSet.expressions[i], values, value));
                                solutionsFound.incrementAndGet();
                            }
                        }
                    }
                }
                return null;
            }));
        }

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Error evaluating expressions", e);
            }
        }

        executor.shutdown();
        return new SolutionList(new EvaluatedExpressionList(evaluatedExpressions.toArray(new EvaluatedExpression[0])), goal);
    }
}
