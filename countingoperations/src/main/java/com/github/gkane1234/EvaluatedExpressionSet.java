package com.github.gkane1234;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
/**
    This class is used to represent a list of solutions.
*/
public class EvaluatedExpressionSet {
    private HashSet<EvaluatedExpression> evaluatedExpressionSet;
    private double[] values;
    /**
        Constructor for the EvaluatedExpressionSet class.
        @param EvaluatedExpressionList: an <code>EvaluatedExpression[]</code> representing the list of solutions to add to the set.
    */
    public EvaluatedExpressionSet(EvaluatedExpression[] EvaluatedExpressionList) {
        this.evaluatedExpressionSet=new HashSet<>();
        for (EvaluatedExpression evaluatedExpression : EvaluatedExpressionList) {
            this.evaluatedExpressionSet.add(evaluatedExpression);
        }
        if (EvaluatedExpressionList.length > 0) {
            values = EvaluatedExpressionList[0].getValues();
        } else {
            values = null;
        }
    }

    /**
        Constructor for the EvaluatedExpressionList class.
        @param values: a <code>double[]</code> representing the values used in the equation.
    */
    public EvaluatedExpressionSet(double[] values) {
        this.evaluatedExpressionSet=new HashSet<>();
        this.values=values;
    }
    /**
        Constructor for the EvaluatedExpressionList class.
        @param values: a <code>int[]</code> representing the values used in the equation.
    */
    public EvaluatedExpressionSet(int[] values) {
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
        this.evaluatedExpressionSet.add(toAdd);
    }
    /**
        Returns the number of solutions in the solution set.
        @return an <code>int</code> representing the number of solutions in the set.
    */
    public int getNumEvaluatedExpressions(){
        return this.evaluatedExpressionSet.size();
    }
    /**
        Returns the values used in the equation.
        @return a <code>double[]</code> representing the values used in the equation.
    */
    public double[] getValues() {
        return this.values;
    }

    public List<EvaluatedExpression> getEvaluatedExpressionList() {
        return new ArrayList<>(this.evaluatedExpressionSet);
    }

    public EvaluatedExpression getEvaluatedExpression() {
        return this.evaluatedExpressionSet.iterator().next();
    }
    /**
        Returns a string representation of the solution set.
        @return a <code>String</code> representing the solution set.
    */

    public EvaluatedExpression getEvaluatedExpression(int index) {
        return (EvaluatedExpression) this.evaluatedExpressionSet.toArray()[index];
    }
    @Override
    public String toString() {
        String toReturn = "{";
        for (double value : values) {
            toReturn+=String.valueOf(value);
            toReturn+=",";
        }
        toReturn+="} ";
        toReturn+= "Found "+String.valueOf(getNumEvaluatedExpressions())+" EvaluatedExpression(s): ";

        for (EvaluatedExpression evaluatedExpression : this.evaluatedExpressionSet) {
            toReturn+=evaluatedExpression.toString()+", ";
        }
        return toReturn;
    }
}
