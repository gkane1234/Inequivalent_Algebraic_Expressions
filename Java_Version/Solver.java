package counting_operations.Java_Version;

import java.util.ArrayList;
import java.util.Random;

public class Solver {
    private static final double TOLERANCE = 1e-7;
    ExpressionList solverList;
    private int numValues;
    Solver(int numValues){
        solverList = new ExpressionDynamic(numValues,5,1,null).getExpressionList();
        this.numValues=numValues;
    }

    public ArrayList<Solution> findAllSolutions(double[] values, double goal) {
        ArrayList<Solution> solutions = new ArrayList<>();
        for (Expression expression:solverList.getExpressions()) {
            if (Math.abs(expression.evaluate_with_values(values)-goal)<=TOLERANCE) {
                solutions.add(new Solution(expression,values));
            }
        }
        return solutions;
    }

    public ArrayList<Double[]> findSolvableValues(int[] valueRange, int[] solutionRange) {
        //TODO: Implement
        Random r = new Random();
        int[] nextAttempt =r.ints(numValues, valueRange[0],valueRange[1]).toArray();
        return null;
    } 
}
