package com.github.gkane1234;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.FileNotFoundException;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Arrays;
import javax.swing.SwingWorker;
/**
    Solver class for finding solutions for a given goal using a set of values.
*/
public class Solver extends SwingWorker<Void,String>{
    private static final int NUM_TRUNCATORS = 20;
    private static final int ROUNDING =9;
    private static final double TOLERANCE = 1e-5;

    private static final int MAX_SOLUTIONS = 200;
    ExpressionSet solverSet;
    private int numValues;
    private boolean verbose;

    private final CountingOperationsApplet applet;

    private String status;
    /**
        Constructor for the Solver class.
        @param numValues: an <code>int</code> representing the number of values to use.
        @param verbose: a <code>boolean</code> representing whether to print verbose output.
    */
    Solver(int numValues,boolean verbose, boolean load, CountingOperationsApplet applet){
        this.verbose = verbose;
        this.applet = applet;
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
    @Override
    protected Void doInBackground() {
        publish(status);
        return null;

    }
    @Override
    protected void process(List<String> chunks) {
        for (String chunk : chunks) {
            applet.onSolverUpdate(chunk);
        }
    }
    /**
        Finds all solutions for a given goal using a set of values.
        @param values: an <code>double[]</code> representing the values to use.
        @param goal: a <code>double</code> representing the goal to find solutions for.
        @return a <code>SolutionSet</code> representing the solutions found.
    */
    public SolutionList findAllSolutions(double[] values, double goal,int maxSolutions) {
        return ExpressionSet.findSolutions(solverSet, values, goal, Solver.ROUNDING, maxSolutions);
    }
    /**
        Finds all solutions for a given goal using a set of values.
        @param values: an <code>int[]</code> representing the values to use.
        @param goal: a <code>double</code> representing the goal to find solutions for.
        @return a <code>SolutionSet</code> representing the solutions found.
    */
    public SolutionList findAllSolutions(int[] values, double goal,int maxSolutions) {
        double[] doubleValues = new double[values.length];
        for (int i=0;i<values.length;i++) {
            doubleValues[i]=values[i];
        }
        return findAllSolutions(doubleValues,goal,maxSolutions);

    }
    /**
        Finds upto one solution for a given goal using a set of values.
        @param values: an <code>double[]</code> representing the values to use.
        @param goal: a <code>double</code> representing the goal to find solutions for.
        @return a <code>Solution</code> representing the first solution found.
    */
    public EvaluatedExpression findFirstSolution(double[] values, double goal) {
        for (int i=0;i<solverSet.getNumExpressions();i++) {
            if (verbose&&i%100000==0) {
                publish("Evaluating expression "+i+" of "+solverSet.getNumExpressions());
            }
            if (Solver.equal(solverSet.get(i).evaluateWithValues(values,Solver.ROUNDING),goal)) {
                return new EvaluatedExpression(solverSet.get(i),values,goal);
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
    public EvaluatedExpression findFirstSolution(int[] values, double goal) {
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
        @return a <code>List<SolutionList></code> representing the solutions found.
    */
    public List<SolutionList> findSolvableValues(int numSolutions,double goal, int[] valueRange, int[] solutionRange) throws Exception {
        final int MAX_ATTEMPTS = 1000;
        Random r = new Random();

        List<SolutionList> solvables = new ArrayList<>();

        int attempts =0;
    
        while(solvables.size()<numSolutions&&attempts<MAX_ATTEMPTS) {
            
            attempts++;
            int[] nextAttempt = r.ints(numValues, valueRange[0], valueRange[1]).toArray();
            if (this.verbose) {
                publish("Attempting: "+attempts);
                publish(" ");
                publish(Arrays.toString(nextAttempt));
            }
    
            SolutionList solutions = findAllSolutions(nextAttempt, goal,solutionRange[1]+1);
            
            if (solutionRange[0] <= solutions.getNumSolutions() && solutions.getNumSolutions() <= solutionRange[1]) {
                solvables.add(solutions);
                if (this.verbose) {
                    publish(String.valueOf(attempts));
                }
                attempts=0;
            }
        }

        if (attempts==MAX_ATTEMPTS) {
            throw new Exception("Constraints not met within "+MAX_ATTEMPTS+" attempts");
        }
    
        return solvables; 
    }
    /**
        Finds a random set of values that can be used to make a given goal.
        @param numSolutions: an <code>int</code> representing the number of solutions to find.
        @return a <code>List<SolutionSet></code> representing the solutions found.
    */
    public List<SolutionList> findSolvableValues(int numSolutions,double goal) throws Exception {
        int[] defualtRange = new int[]{1,15};
        int[] defaultSolutionRange = new int[]{1,100*this.numValues};
        return findSolvableValues(numSolutions,goal,defualtRange,defaultSolutionRange);
    }
    /**
        Finds all possible sets of values that can be used to make a given goal.
        @param range: an <code>int[]</code> representing the range of values to use.
        @param goal: a <code>double</code> representing the goal to find solutions for.
        @param findAllSolutions: a <code>boolean</code> representing whether to find all solutions or just one.
        @return a <code>List<SolutionSet></code> representing the solutions found.
    */
    public List<SolutionList> findAllPossibleSolvableValuesInRange(int[] range,double goal,boolean findAllSolutions) {
        //Finds all possible solvable sets of numbers to make the goal
        // Will do so only returning values in non-descreasing order
        RangeIterator allValuesInRangeIterator = new RangeIterator(range, this.numValues);
        List<SolutionList> solvables = new ArrayList<>();
        SolutionList nextSolutionList;
        int tracker = 0;
        while (allValuesInRangeIterator.hasNext()) {
            int[] values = allValuesInRangeIterator.next();
            if (++tracker%1000==0) {
                CountingMain.print(values);
            }
            if (findAllSolutions) {
                nextSolutionList = findAllSolutions(values, goal,200);
            } else {
                EvaluatedExpression firstSolution = findFirstSolution(values, goal);
                nextSolutionList = new SolutionList(values,goal);
                if (firstSolution!=null) {
                    nextSolutionList.addEvaluatedExpression(firstSolution);
                }
            }
            solvables.add(nextSolutionList);
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
        TIntObjectHashMap <EvaluatedExpression> outputs = new TIntObjectHashMap<>();


        EvaluatedExpressionList evaluatedExpressionList = ExpressionSet.evaluate(solverSet, values, Solver.ROUNDING);
        for (EvaluatedExpression evaluatedExpression : evaluatedExpressionList.getEvaluatedExpressionList()) {
            double answer = evaluatedExpression.getValue();
            if (Solver.equal(answer, Math.round(answer))) {
                int wholeNumberAnswer = (int)Math.round(answer);
                
                //System.err.println(wholeNumberAnswer);
                if (intSet.add(wholeNumberAnswer)&&output) {
                    outputs.put(wholeNumberAnswer, evaluatedExpression);
                }
            }
            
            
        }
        int i = goalRange[0];
        int delta = goalRange[0]<goalRange[1]? 1: -1;
        while (true){
            boolean inside  = intSet.contains(i);
            i+=delta;
            if (output) {
                    publish(String.valueOf(i));
                    publish(" ");
                    publish(outputs.get(i).toString());

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
    public static boolean equal(double a, double b) {
        return Math.abs(a-b)<=TOLERANCE;
    }
}
