package com.github.gkane1234;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

public class Solver {
    private static final double TOLERANCE = 1e-5;
    private static final int MAX_ATTEMPTS = 1000;
    private static final int MAX_SOLUTIONS = 200;
    ExpressionSet solverSet;
    private int numValues;
    Solver(int numValues){
        solverSet = new ExpressionDynamic(numValues,7,5,null).getExpressionSet();
        System.err.println(solverSet.size());
        this.numValues=numValues;
    }

    public SolutionSet findAllSolutions(double[] values, double goal) {
        SolutionSet solutions = new SolutionSet(values,goal);
        for (int i=0;i<solverSet.getNumExpressions();i++) {
            if (Solver.equal(solverSet.get(i).evaluateWithValues(values,solverSet.rounding),goal)) {
                    solutions.addSolution(new Solution(solverSet.get(i),values,goal));
            }
            if (solutions.getNumSolutions()>MAX_SOLUTIONS) {
                i=solverSet.getNumExpressions();
            }
        }
        return solutions;
    }

    public SolutionSet findAllSolutions(int[] values, double goal) {
        double[] doubleValues = new double[values.length];
        for (int i=0;i<values.length;i++) {
            doubleValues[i]=values[i];
        }
        return findAllSolutions(doubleValues,goal);

    }

    public Solution findFirstSolution(double[] values, double goal) {
        for (int i=0;i<solverSet.getNumExpressions();i++) {
            if (Solver.equal(solverSet.get(i).evaluateWithValues(values,solverSet.rounding),goal)) {
                return new Solution(solverSet.get(i),values,goal);
            }
        }
        return null;
    }
    
    public Solution findFirstSolution(int[] values, double goal) {
        double[] doubleValues = new double[values.length];
        for (int i=0;i<values.length;i++) {
            doubleValues[i]=values[i];
        }
        return findFirstSolution(doubleValues, goal);

    }

    public List<SolutionSet> findSolvableValues(int numSolutions,double goal, int[] valueRange, int[] solutionRange) {
        Random r = new Random();

        List<SolutionSet> solvables = new ArrayList<>();

        int attempts =0;
    
        while(solvables.size()<numSolutions&&attempts<MAX_ATTEMPTS) {
            attempts++;
            int[] nextAttempt = r.ints(numValues, valueRange[0], valueRange[1]).toArray();
    
            SolutionSet solutions = findAllSolutions(nextAttempt, goal);
            
            if (solutionRange[0] <= solutions.getNumSolutions() && solutions.getNumSolutions() <= solutionRange[1]) {
                solvables.add(solutions);
                System.out.println(attempts);
                attempts=0;
            }
        }
    
        return solvables; 
    }
    public List<SolutionSet> findSolvableValues(int numSolutions) {
        int[] defualtRange = new int[]{1,15};
        int[] defaultSolutionRange = new int[]{1,100*this.numValues};
        double defaultGoal = 24d;
        return findSolvableValues(numSolutions,defaultGoal,defualtRange,defaultSolutionRange);
    }

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

    public int findFirstInRange(double[] values,int[] valueRange,boolean works,boolean output) {
        // If works is true: Returns the first value that can be created using the values
        // If works is false: Returns the first value that cannot be created if works
        TIntHashSet intSet = new TIntHashSet();
        TIntObjectHashMap <Solution> outputs = new TIntObjectHashMap<>();

        for (int i=0;i<solverSet.getNumExpressions();i++) {
            double answer = solverSet.get(i).evaluateWithValues(values,solverSet.rounding);
            if (Solver.equal(answer, Math.round(answer))) {
                int wholeNumberAnswer = (int)Math.round(answer);
                
                //System.err.println(wholeNumberAnswer);
                if (intSet.add(wholeNumberAnswer)&&output) {
                    outputs.put(wholeNumberAnswer, new Solution(solverSet.get(i), values, wholeNumberAnswer));
                }
            }
            
            
        }
        int i = valueRange[0];
        int delta = valueRange[0]<valueRange[1]? 1: -1;
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

    private static boolean equal(double a, double b) {
        return Math.abs(a-b)<=TOLERANCE;
    }
}
