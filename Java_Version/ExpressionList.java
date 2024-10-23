package counting_operations.Java_Version;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;
public class ExpressionList {
    private List<Expression> expressions;
    public int numValues;
    public int rounding;
    public boolean genericExpressions;

    private List<Set<Double>> seen;
    private double[][] truncators;
    public int numTruncators;
    public Operation[] ops;

    // Constructor
    public ExpressionList(List<Expression> expressions, int numValues, int rounding, int numTruncators,boolean genericExpressions) {
        this.expressions = expressions != null ? new ArrayList<>(expressions) : new ArrayList<>();
        this.numValues = numValues;
        this.rounding = rounding;
        this.genericExpressions = genericExpressions;

        this.seen = new ArrayList<>();
        this.truncators = new double[numTruncators][numValues];
        this.numTruncators = numTruncators;
        
        Random random = new Random();
        for (int i = 0; i < numTruncators; i++) {
            Set<Double> set = new HashSet<>();
            seen.add(set);
            double[] truncator = new double[numValues];
            for (int j = 0; j < numValues; j++) {
                truncator[j] = random.nextDouble();
            }
            truncators[i]=truncator;
        }

    }
    public int getNumValues() {
        return this.numValues;
    }
    // Getter for a specific expression (equivalent to __getitem__)
    public Expression get(int i) {
        return expressions.get(i);
    }

    // Length of the expression list (equivalent to __len__)
    public int size() {
        return expressions.size();
    }

    // Iterating over expressions (equivalent to __iter__)
    public List<Expression> getExpressions() {
        return expressions;
    }

    // Hash code (equivalent to __hash__)
    @Override
    public int hashCode() {
        return expressions.hashCode();
    }

    // Equals method (equivalent to __eq__)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExpressionList other = (ExpressionList) obj;
        return expressions.equals(other.expressions);
    }

    // String representation (equivalent to __str__ and __repr__)
    @Override
    public String toString() {
        return expressions.toString();
    }

    public ExpressionList evaluate(int[] values) {
        return createEvaluatedExpressionList(this, values, this.numTruncators);
    }

    // Add an expression to the list
    public boolean add(Expression expression) {
        boolean toAdd = false;
        for (int i = 0; i < this.numTruncators; i++) {
            double value = expression.evaluate_with_values(this.truncators[i]);
            if (!Double.isNaN(value) && seen.get(i).add(value)) {
                toAdd = true;  
            }
        }
        if (toAdd) {
            expressions.add(expression);
        }
        return toAdd;
    }

    // Get the values of the expressions
    public List<Number> getValues() throws IllegalStateException {
        if (genericExpressions) {
            throw new IllegalStateException("Expression list is generic");
        }

        List<Number> values = new ArrayList<>();
        for (Expression expression : expressions) {
            values.add(expression.getValue());
        }
        return values;
    }

    // Static method to create a new expression list with the given values
    public static ExpressionList createEvaluatedExpressionList(ExpressionList genericExpressionList, int[] values, int numTruncators) throws IllegalStateException {
        if (!genericExpressionList.genericExpressions) {
            throw new IllegalStateException("Input must be a generic expression list");
        }

        List<Expression> newExpressions = new ArrayList<>();
        for (Expression expression : genericExpressionList.getExpressions()) {
            newExpressions.add(expression.change_values(values));
        }
        
        return new ExpressionList(newExpressions, genericExpressionList.numValues, genericExpressionList.rounding, numTruncators,true);
    }

}