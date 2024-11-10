package com.github.gkane1234;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.FileNotFoundException;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Arrays;
/**
    Solver class for finding solutions for a given goal using a set of values.
*/
public class Solver {
    private static final int NUM_TRUNCATORS = 20;
    private static final int ROUNDING =9;
    private static final double TOLERANCE = 1e-5;
    private static final int MAX_ATTEMPTS = 1000;
    private static final int MAX_SOLUTIONS = 200;
    ExpressionSet solverSet;
    private int numValues;
    private boolean verbose;
    /**
        Constructor for the Solver class.
        @param numValues: an <code>int</code> representing the number of values to use.
        @param verbose: a <code>boolean</code> representing whether to print verbose output.
    */
    Solver(int numValues,boolean verbose, boolean load){
        this.verbose = verbose;
        if (load) {
            try {
                if (verbose) {
                    System.err.println("Loading expression set...");
                }
                solverSet = ExpressionSet.load(numValues, Counter.run(numValues).intValue(),verbose);
            } catch (FileNotFoundException e) {
                if (verbose) {
                    System.err.println("File not found, creating instead...");
                }
                solverSet = new ExpressionDynamic(numValues,ROUNDING,NUM_TRUNCATORS,null,verbose).getExpressionSet();
                ExpressionSet.save(solverSet,verbose);
            }
            
        } else {
            solverSet = new ExpressionDynamic(numValues,ROUNDING,NUM_TRUNCATORS,null,verbose).getExpressionSet();
        }
        if (verbose) {
            System.err.println("Loaded "+solverSet.getNumExpressions()+" expressions.");
        }
        this.numValues=numValues;
    }
    /**
        Finds all solutions for a given goal using a set of values.
        @param values: an <code>double[]</code> representing the values to use.
        @param goal: a <code>double</code> representing the goal to find solutions for.
        @return a <code>SolutionSet</code> representing the solutions found.
    */
    public SolutionSet findAllSolutions(double[] values, double goal) {
        SolutionSet solutions = new SolutionSet(values,goal);
        for (int i=0;i<solverSet.getNumExpressions();i++) {
            if (Solver.equal(solverSet.get(i).evaluateWithValues(values,Solver.ROUNDING),goal)) {
                    solutions.addSolution(new Solution(solverSet.get(i),values,goal));
            }
            if (solutions.getNumSolutions()>MAX_SOLUTIONS) {
                i=solverSet.getNumExpressions();
            }
        }
        return solutions;
    }
    /**
        Finds all solutions for a given goal using a set of values.
        @param values: an <code>int[]</code> representing the values to use.
        @param goal: a <code>double</code> representing the goal to find solutions for.
        @return a <code>SolutionSet</code> representing the solutions found.
    */
    public SolutionSet findAllSolutions(int[] values, double goal) {
        double[] doubleValues = new double[values.length];
        for (int i=0;i<values.length;i++) {
            doubleValues[i]=values[i];
        }
        return findAllSolutions(doubleValues,goal);

    }
    /**
        Finds upto one solution for a given goal using a set of values.
        @param values: an <code>double[]</code> representing the values to use.
        @param goal: a <code>double</code> representing the goal to find solutions for.
        @return a <code>Solution</code> representing the first solution found.
    */
    public Solution findFirstSolution(double[] values, double goal) {
        for (int i=0;i<solverSet.getNumExpressions();i++) {
            if (Solver.equal(solverSet.get(i).evaluateWithValues(values,Solver.ROUNDING),goal)) {
                return new Solution(solverSet.get(i),values,goal);
            }
        }
        return null;
    }
    /**
        Finds upto one solution for a given goal using a set of values.
        @param values: an <code>int[]</code> representing the values to use.
        @param goal: a <code>double</code> representing the goal to find solutions for.
        @return a <code>Solution</code> representing the first solution found.
    */
    public Solution findFirstSolution(int[] values, double goal) {
        double[] doubleValues = new double[values.length];
        for (int i=0;i<values.length;i++) {
            doubleValues[i]=values[i];
        }
        return findFirstSolution(doubleValues, goal);

    }

    /**
        Finds a random set of values that can be used to make a given goal.
        @param numSolutions: an <code>int</code> representing the number of solutions to find.
        @param goal: a <code>double</code> representing the goal to find solutions for.
        @param valueRange: an <code>int[]</code> representing the range of values to use.
        @param solutionRange: an <code>int[]</code> representing the range of solutions to find.
        @return a <code>List<SolutionSet></code> representing the solutions found.
    */
    public List<SolutionSet> findSolvableValues(int numSolutions,double goal, int[] valueRange, int[] solutionRange) {
        Random r = new Random();

        List<SolutionSet> solvables = new ArrayList<>();

        int attempts =0;
    
        while(solvables.size()<numSolutions&&attempts<MAX_ATTEMPTS) {
            
            attempts++;
            int[] nextAttempt = r.ints(numValues, valueRange[0], valueRange[1]).toArray();
            if (this.verbose) {
                System.out.print("Attempting: "+attempts);
                System.out.print(" ");
                System.out.println(Arrays.toString(nextAttempt));
            }
    
            SolutionSet solutions = findAllSolutions(nextAttempt, goal);
            
            if (solutionRange[0] <= solutions.getNumSolutions() && solutions.getNumSolutions() <= solutionRange[1]) {
                solvables.add(solutions);
                if (this.verbose) {
                    System.out.println(attempts);
                }
                attempts=0;
            }
        }
    
        return solvables; 
    }
    /**
        Finds a random set of values that can be used to make a given goal.
        @param numSolutions: an <code>int</code> representing the number of solutions to find.
        @return a <code>List<SolutionSet></code> representing the solutions found.
    */
    public List<SolutionSet> findSolvableValues(int numSolutions) {
        int[] defualtRange = new int[]{1,15};
        int[] defaultSolutionRange = new int[]{1,100*this.numValues};
        double defaultGoal = 24d;
        return findSolvableValues(numSolutions,defaultGoal,defualtRange,defaultSolutionRange);
    }
    /**
        Finds all possible sets of values that can be used to make a given goal.
        @param range: an <code>int[]</code> representing the range of values to use.
        @param goal: a <code>double</code> representing the goal to find solutions for.
        @param findAllSolutions: a <code>boolean</code> representing whether to find all solutions or just one.
        @return a <code>List<SolutionSet></code> representing the solutions found.
    */
    public List<SolutionSet> findAllPossibleSolvableValuesInRange(int[] range,double goal,boolean findAllSolutions) {
        //Finds all possible solvable sets of numbers to make the goal
        // Will do so only returning values in non-descreasing order
        RangeIterator allValuesInRangeIterator = new RangeIterator(range, this.numValues);
        List<SolutionSet> solvables = new ArrayList<>();
        SolutionSet nextSolutionSet;
        int tracker = 0;
        while (allValuesInRangeIterator.hasNext()) {
            int[] values = allValuesInRangeIterator.next();
            if (++tracker%1000==0) {
                CountingMain.print(values);
            }
            if (findAllSolutions) {
                nextSolutionSet = findAllSolutions(values, goal);
            } else {
                Solution firstSolution = findFirstSolution(values, goal);
                nextSolutionSet = new SolutionSet(values,goal);
                if (firstSolution!=null) {
                    nextSolutionSet.addSolution(firstSolution);
                }
            }
            solvables.add(nextSolutionSet);
        }

        return solvables;

        




    }
    /**
        Finds the first value in a given range that can/can't be created using a set of values.
        @param values: an <code>double[]</code> representing the values to use.
        @param goalRange: an <code>int[]</code> representing the range of goals to use.
        @param works: a <code>boolean</code> representing whether to find the first value that can be created or the first value that cannot be created.
        @param output: a <code>boolean</code> representing whether to print the value and the solution that creates it.
        @return an <code>int</code> representing the first value in the range that can be created or cannot be created.
    */
    public int findFirstInRange(double[] values,int[] goalRange,boolean works,boolean output) {
        // If works is true: Returns the first value that can be created using the values
        // If works is false: Returns the first value that cannot be created using the values
        TIntHashSet intSet = new TIntHashSet();
        TIntObjectHashMap <Solution> outputs = new TIntObjectHashMap<>();

        for (int i=0;i<solverSet.getNumExpressions();i++) {
            double answer = solverSet.get(i).evaluateWithValues(values,Solver.ROUNDING);
            if (Solver.equal(answer, Math.round(answer))) {
                int wholeNumberAnswer = (int)Math.round(answer);
                
                //System.err.println(wholeNumberAnswer);
                if (intSet.add(wholeNumberAnswer)&&output) {
                    outputs.put(wholeNumberAnswer, new Solution(solverSet.get(i), values, wholeNumberAnswer));
                }
            }
            
            
        }
        int i = goalRange[0];
        int delta = goalRange[0]<goalRange[1]? 1: -1;
        while (true){
            boolean inside  = intSet.contains(i);
            i+=delta;
            if (output) {
                    System.out.print(i);
                    System.out.print(" ");
                    System.out.println(outputs.get(i));

                }
                
            
            if (inside!=works) {
                return i;
            }
        }
    }
    /**
        Checks if two doubles are equal within a given tolerance.
        @param a: a <code>double</code> representing the first value to compare.
        @param b: a <code>double</code> representing the second value to compare.
        @return a <code>boolean</code> representing whether the two values are equal within the given tolerance.
    */
    private static boolean equal(double a, double b) {
        return Math.abs(a-b)<=TOLERANCE;
    }
}
