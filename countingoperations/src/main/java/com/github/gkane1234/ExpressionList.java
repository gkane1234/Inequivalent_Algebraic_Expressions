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
public class ExpressionList implements Serializable{
    private static final long serialVersionUID = 1L;
    protected static final int BUFFER_SIZE = 1024*1024;

    protected static final String FILE_PATH = "counting_operations/outputs/";

    protected Expression[] expressions;
    protected int numExpressions;
    protected int numValues;

    protected static final int THREASHOLD = 2;

    /**
        Constructor for an ExpressionSet.
        @param expressions: expressions that have already been found to be inequivalent.
        @param numExpressions: an <code>int</code> representing the number of expressions that are in expressions. (This can differ from the length of expressions)
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param rounding: an <code>int</code> representing the number of decimal places to round to.
        @param numTruncators: an <code>int</code> representing the number of truncators to use.
    */
    
    public ExpressionList(Expression[] expressions,int numExpressions,int numValues) {
        
        this.expressions=expressions;
        this.numExpressions=numExpressions;
        this.numValues = numValues;

    }

    protected ExpressionList() {

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
        return Arrays.toString(getExpressions());
    }
    /**
        Creates a new ExpressionSet with a new value order without modifying the original set.
        @param expressionSet: an <code>ExpressionSet</code> to create a new set from.
        @param value_order: a <code>byte[]</code> representing the new value order.
    */
    public ExpressionList changeValueOrder(byte[] valueOrder) throws IllegalStateException {
        
        Expression[] newExpressions = new Expression[this.numExpressions];
        for (int i=0;i<this.numExpressions;i++) {
            newExpressions[i]=this.get(i).changeValueOrder(valueOrder);
            
        }
        return new ExpressionList(newExpressions,this.numExpressions,this.numValues); // 0 truncators since we dont want them to be initialized where this is used
    }

    public boolean add(Expression expression) {
        if (this.numExpressions==this.expressions.length) {
            throw new IllegalStateException("ExpressionList is full");
        }
        expressions[this.numExpressions++]=expression;
        return true;
    }

    /**
        Adds an expression to the set without checking if it is equivalent to any of the expressions already in the list.
        For expressionLists this is exactly the same as add.
        @param expression: an <code>Expression</code> to add to the set.
    */
    public void forceAdd(Expression expression) {
        if (this.numExpressions==this.expressions.length) {
            throw new IllegalStateException("ExpressionList is full");
        }
        expressions[this.numExpressions++]=expression;
    }

    @Override
    public boolean equals(Object o) {
        //System.err.println(this);
        //System.err.println(o);
        if (o instanceof ExpressionList) {
            ExpressionList e = (ExpressionList)o;
            if (this.numExpressions!=e.numExpressions) {
                return false;
            }
            for (int i = 0; i < this.numExpressions; i++) {
                //System.out.println(this.expressions[i]+" "+e.expressions[i]);
                if (!this.expressions[i].equals(e.expressions[i])) {
                    return false;
                }

            }
            return true;
        }
        return false;
    }

    public void cleanup() {
        // nothing to do
    }

    /**
        Saves an expression set to a file.
        @param expressionList: an <code>ExpressionList</code> to save.
        @param verbose: a <code>boolean</code> representing whether to print progress.
    */
    public static void save(ExpressionList expressionList, boolean verbose) {
        final int BUFFER_SIZE = 1024*1024;
        String filename = FILE_PATH + "expressionList_" + expressionList.numValues + "_" + expressionList.numExpressions + ".ser";
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename),BUFFER_SIZE);
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            for (int i = 0; i < expressionList.numExpressions; i++) {
                if (verbose&&i%1000000==0) {
                    System.out.println("Saving expression "+i);
                }
                oos.writeObject(expressionList.expressions[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveCompressed(ExpressionList expressionList, boolean verbose) {
        final int BUFFER_SIZE = 1024*1024;
        if (verbose) {
            System.err.println("Compressing expression set");
        }
        long[] compressedExpressions = ExpressionCompression.compressExpressionList(expressionList);
        String filename = getCompressedFilename(expressionList.numValues);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename),BUFFER_SIZE);
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                if (verbose) {
                    System.out.println("Saving compressed expression set");
                }
                oos.writeObject(compressedExpressions);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ExpressionList loadCompressed(String filename, int numValues,int numExpressions, boolean verbose) throws FileNotFoundException {
        long startTime = System.currentTimeMillis();
        if (verbose) {  
            System.err.println(filename);
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename),BUFFER_SIZE);
            ObjectInputStream ois = new ObjectInputStream(bis)) {
            long[] compressedExpressions = (long[]) ois.readObject();
            long endTime = System.currentTimeMillis();
            if (verbose) {
                System.out.println("Loaded compressed expression set in "+(endTime-startTime)/1000.0+" seconds");
            }
            return ExpressionCompression.decompressExpressionList(compressedExpressions,numExpressions,numValues,verbose);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new FileNotFoundException("File not found: " + filename);
        }


    }
    public static ExpressionList loadCompressed(int numValues) throws FileNotFoundException {
        String filename = getCompressedFilename(numValues);
        return loadCompressed(filename, numValues, getMaximumSize(numValues), true);
    }

    public static String getCompressedFilename(int numValues) {
        return FILE_PATH+"expressionList_"+numValues+"_"+getMaximumSize(numValues)+"_compressed.ser";
    }
    /**
        Loads the expression list from a file which contains all algebraically inequivalent expressions with a given number of values.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @return an <code>ExpressionList</code> representing the loaded list.
    */
    public static ExpressionList load(int numValues) throws FileNotFoundException {
        int numExpressions = getMaximumSize(numValues);
        String filename = FILE_PATH+"expressionList_"+numValues+"_"+numExpressions+".ser";
        return load(filename, numValues, numExpressions, true);
    }
    /**
        Loads the expression list from a file which contains numExpressions inequivalent expressions with a given number of values.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param numExpressions: an <code>int</code> representing the number of expressions in the list.
        @return an <code>ExpressionList</code> representing the loaded list.
    */
    public static ExpressionList load(int numValues, int numExpressions) throws FileNotFoundException {
        String filename = FILE_PATH+"expressionList_"+numValues+"_"+numExpressions+".ser";
        return load(filename, numValues, numExpressions, true);

    }
    /**
        Loads the expression list from a file which contains numExpressions inequivalent expressions with a given number of values.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param numExpressions: an <code>int</code> representing the number of expressions in the list.
        @param verbose: a <code>boolean</code> representing whether to print progress.
        @return an <code>ExpressionList</code> representing the loaded list.
    */
    public static ExpressionList load(int numValues, int numExpressions, boolean verbose) throws FileNotFoundException {
        String filename = FILE_PATH+"expressionList_"+numValues+"_"+numExpressions+".ser";
        return load(filename, numValues, numExpressions, verbose);
    }
    /**
        Loads the expression set from a file which contains numExpressions inequivalent expressions with a given number of values.
        @param filename: a <code>String</code> representing the path to the file containing the expressions.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param numExpressions: an <code>int</code> representing the number of expressions in the list.
        @param verbose: a <code>boolean</code> representing whether to print progress.
        @return an <code>ExpressionList</code> representing the loaded list.
    */
    public static ExpressionList load(String filename, int numValues,int numExpressions, boolean verbose) throws FileNotFoundException {
        // Use a thread-safe list to store expressions
        long startTime = System.currentTimeMillis();

        int decile = Math.max(numExpressions/10, 1);
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
            System.err.println(e);
            throw new FileNotFoundException("File not found: " + filename);
        }
        long endTime = System.currentTimeMillis();
        if (verbose) {
            System.out.println("Loaded "+numExpressions+" expressions in "+((endTime-startTime)/1000.0)+" seconds");
        }
        return new ExpressionList(expressions, numExpressions, numValues);
    }



    
    public static EvaluatedExpressionList evaluate(ExpressionList expressionList, double[] values, int rounding) {
        final int numThreads = 20;

        EvaluatedExpression[] evaluatedExpressions = new EvaluatedExpression[expressionList.getNumExpressions()];
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Void>> futures = new ArrayList<>();
        AtomicInteger evaluatedExpressionsCount = new AtomicInteger(0);

        int chunkSize = (expressionList.getNumExpressions() + numThreads - 1) / numThreads;
        for (int t = 0; t < numThreads; t++) {
            final int start = t * chunkSize;
            final int end = Math.min(start + chunkSize, expressionList.getNumExpressions());
            final int decile = expressionList.getNumExpressions()/10;
            futures.add(executor.submit(() -> {
                for (int i = start; i < end; i++) {
                    double value = expressionList.get(i).evaluateWithValues(values, rounding);
                    evaluatedExpressions[i] = new EvaluatedExpression(expressionList.get(i), values, value);
                    evaluatedExpressionsCount.incrementAndGet();
                    if (evaluatedExpressionsCount.get()%decile==0) {
                        System.out.println("Evaluated "+evaluatedExpressionsCount.get()+" expressions");
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
        return new EvaluatedExpressionList(evaluatedExpressions);
    }

    public static SolutionList findSolutions(ExpressionList expressionList, double[] values, double goal, int rounding, int maxSolutions, boolean verbose) {
        final int numThreads = 10;
        long startTime = System.currentTimeMillis();
        SolutionList solutions = new SolutionList(values,goal);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Void>> futures = new ArrayList<>();
        
        final Object lock = new Object(); // For thread-safe list access
        AtomicInteger solutionsFound = new AtomicInteger(0);
        final int decile = expressionList.getNumExpressions()/10;

        AtomicInteger evaluatedExpressionsCount = new AtomicInteger(0);

        int chunkSize = (expressionList.getNumExpressions() - 1) / numThreads+1;
        for (int t = 0; t < numThreads; t++) {
            final int start = t * chunkSize;
            final int end = Math.min(start + chunkSize, expressionList.getNumExpressions());
            
            futures.add(executor.submit(() -> {
                for (int i = start; i < end && solutionsFound.get() < maxSolutions; i++) {
                    
                    double value = expressionList.get(i).evaluateWithValues(values, rounding);
                    if (verbose) {
                        evaluatedExpressionsCount.incrementAndGet();
                        if (evaluatedExpressionsCount.get()%decile==0) {
                            System.out.println("Evaluated "+evaluatedExpressionsCount.get()+" expressions");
                        }
                    }
                    if (Solver.equal(value, goal)) { // Check if value equals goal
                        
                        synchronized(lock) {
                            if (solutionsFound.get() < maxSolutions) {
                                solutions.addEvaluatedExpression(new EvaluatedExpression(expressionList.get(i), values, value));
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
        long endTime = System.currentTimeMillis();
        return solutions;
    }
}
