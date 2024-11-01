package com.github.gkane1234;
import java.util.List;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class SolutionSet {
    private List<Solution> solutionSet;
    private double goal;
    private double[] values;
    public SolutionSet(List<Solution> solutionSet) {
        this.solutionSet=solutionSet;
        goal = solutionSet.get(0).getGoal();
        values = solutionSet.get(0).getValues();
    }
    private SolutionSet(double goal) {
        this.solutionSet= new ArrayList<>(1);
        this.goal=goal;
    }
    public SolutionSet(double[] values,double goal) {
        this(goal);
        this.values=values;
    }
    public SolutionSet(int[] values, double goal) {
        this(goal);
        double[] doubleValues = new double[values.length];
        for (int i=0;i<values.length;i++) {
            doubleValues[i]=values[i];
        }
        this.values=doubleValues;
        
    }
    public void addSolution(Solution toAdd) {
        if (toAdd.getGoal()!=this.goal) {
            throw new InvalidParameterException("Goal must be the same");
        }
        this.solutionSet.add(toAdd);
    }
    public int getNumSolutions(){
        return this.solutionSet.size();
    }
    public double[] getValues() {
        return this.values;
    }
    public double getGoal() {
        return this.goal;
    }
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
