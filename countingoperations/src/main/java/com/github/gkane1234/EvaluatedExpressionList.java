package com.github.gkane1234;
import java.util.List;
import java.util.ArrayList;
/**
    This class is used to represent a list of solutions.
*/
public class EvaluatedExpressionList {
    private List<EvaluatedExpression> EvaluatedExpressionList;
    private double[] values;
    /**
        Constructor for the EvaluatedExpressionList class.
        @param EvaluatedExpressionList: an <code>EvaluatedExpression[]</code> representing the list of solutions to add to the set.
    */
    public EvaluatedExpressionList(EvaluatedExpression[] EvaluatedExpressionList) {
        this.EvaluatedExpressionList=new ArrayList<>(EvaluatedExpressionList.length);
        for (EvaluatedExpression evaluatedExpression : EvaluatedExpressionList) {
            this.EvaluatedExpressionList.add(evaluatedExpression);
        }
        values = EvaluatedExpressionList[0].getValues();
    }

    /**
        Constructor for the EvaluatedExpressionList class.
        @param values: a <code>double[]</code> representing the values used in the equation.
    */
    public EvaluatedExpressionList(double[] values) {
        this.values=values;
    }
    /**
        Constructor for the EvaluatedExpressionList class.
        @param values: a <code>int[]</code> representing the values used in the equation.
    */
    public EvaluatedExpressionList(int[] values) {
        double[] doubleValues = new double[values.length];
        for (int i=0;i<values.length;i++) {
            doubleValues[i]=values[i];
        }
        this.values=doubleValues;
        
    }
    /**
        Adds an <code>EvaluatedExpression</code> to the list.
        @param toAdd: an <code>EvaluatedExpression</code> representing the solution to add to the set.
    */
    public void addEvaluatedExpression(EvaluatedExpression toAdd) {
        this.EvaluatedExpressionList.add(toAdd);
    }
    /**
        Returns the number of solutions in the solution set.
        @return an <code>int</code> representing the number of solutions in the set.
    */
    public int getNumEvaluatedExpressions(){
        return this.EvaluatedExpressionList.size();
    }
    /**
        Returns the values used in the equation.
        @return a <code>double[]</code> representing the values used in the equation.
    */
    public double[] getValues() {
        return this.values;
    }

    public List<EvaluatedExpression> getEvaluatedExpressionList() {
        return this.EvaluatedExpressionList;
    }

    public EvaluatedExpression getEvaluatedExpression(int index) {
        return this.EvaluatedExpressionList.get(index);
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
        toReturn+= "Found "+String.valueOf(getNumEvaluatedExpressions())+" EvaluatedExpression(s): ";

        for (EvaluatedExpression evaluatedExpression : EvaluatedExpressionList) {
            toReturn+=evaluatedExpression.toString()+", ";
        }
        return toReturn;
    }
}
