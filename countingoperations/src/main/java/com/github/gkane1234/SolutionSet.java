package com.github.gkane1234;
import java.util.List;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class SolutionSet {
    private List<Solution> solutionSet;
    private double goal;
    public SolutionSet(List<Solution> solutionSet) {
        this.solutionSet=solutionSet;
        goal = solutionSet.get(0).getGoal();
    }
    public SolutionSet(double goal) {
        this.solutionSet= new ArrayList<>();
        this.goal=goal;
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
    @Override
    public String toString() {
        String toReturn = "Goal of: "+String.valueOf(goal)+". ";

        for (Solution solution : solutionSet) {
            toReturn+=solution.toString()+", ";
        }
        return toReturn;
    }
}
