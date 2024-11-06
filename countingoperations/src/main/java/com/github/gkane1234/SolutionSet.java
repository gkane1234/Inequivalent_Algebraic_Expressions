package com.github.gkane1234;
import java.util.List;
import java.security.InvalidParameterException;
import java.util.ArrayList;
/**
    This class is used to represent a set of solutions to a goal.
*/
public class SolutionSet {
    private List<Solution> solutionSet;
    private double goal;
    private double[] values;
    /**
        Constructor for the SolutionSet class.
        @param solutionSet: a <code>List<Solution></code> representing the list of solutions to add to the set.
    */
    public SolutionSet(List<Solution> solutionSet) {
        this.solutionSet=solutionSet;
        goal = solutionSet.get(0).getGoal();
        values = solutionSet.get(0).getValues();
    }
    /**
        Constructor for the SolutionSet class.
        @param goal: a <code>double</code> representing the goal of the solutions.
    */
    private SolutionSet(double goal) {
        this.solutionSet= new ArrayList<>(1);
        this.goal=goal;
    }
    /**
        Constructor for the SolutionSet class.
        @param values: a <code>double[]</code> representing the values used in the equation.
        @param goal: a <code>double</code> representing the goal of the solutions.
    */
    public SolutionSet(double[] values,double goal) {
        this(goal);
        this.values=values;
    }
    /**
        Constructor for the SolutionSet class.
        @param values: a <code>int[]</code> representing the values used in the equation.
        @param goal: a <code>double</code> representing the goal of the solutions.
    */
    public SolutionSet(int[] values, double goal) {
        this(goal);
        double[] doubleValues = new double[values.length];
        for (int i=0;i<values.length;i++) {
            doubleValues[i]=values[i];
        }
        this.values=doubleValues;
        
    }
    /**
        Adds a solution to the solution set.
        @param toAdd: a <code>Solution</code> representing the solution to add to the set.
    */
    public void addSolution(Solution toAdd) {
        if (toAdd.getGoal()!=this.goal) {
            throw new InvalidParameterException("Goal must be the same");
        }
        this.solutionSet.add(toAdd);
    }
    /**
        Returns the number of solutions in the solution set.
        @return an <code>int</code> representing the number of solutions in the set.
    */
    public int getNumSolutions(){
        return this.solutionSet.size();
    }
    /**
        Returns the values used in the equation.
        @return a <code>double[]</code> representing the values used in the equation.
    */
    public double[] getValues() {
        return this.values;
    }
    /**
        Returns the goal of the solutions.
        @return a <code>double</code> representing the goal of the solutions.
    */
    public double getGoal() {
        return this.goal;
    }
    /**
        Returns a string representation of the solution set.
        @return a <code>String</code> representing the solution set.
    */
    @Override
    public String toString() {
        String toReturn = "{";
        for (double value : values) {
            toReturn+=String.valueOf(value);
            toReturn+=",";
        }
        toReturn+="} ";
        toReturn+= "Found "+String.valueOf(getNumSolutions())+" Solution(s): ";

        for (Solution solution : solutionSet) {
            toReturn+=solution.toString()+", ";
        }
        return toReturn;
    }
}
