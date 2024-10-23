package counting_operations.Java_Version;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpressionDynamic {
    private int rounding;
    private int numValues;
    private int numTruncators;

    // Constructor
    public ExpressionDynamic( int numValues, int rounding, int numTruncators,Operation[] ops) {
        if (ops != null) {
            Operation.changeOperations(ops);
        }
        this.numValues = numValues;
        this.rounding = rounding;
        this.numTruncators = numTruncators;
    }

    // Default constructor
    public ExpressionDynamic() {
        this( 4,5, 3,null);
    }

    // Method to generate the final expression list
    public ExpressionList getExpressionList() {
        // Initialize expression lists
        List<ExpressionList> expressionLists = new ArrayList<>();
        ExpressionList firstExpressionList = new ExpressionList(new ArrayList<>(), 1, rounding, numTruncators,true);
        firstExpressionList.add(new Expression(new int[]{0},new byte[]{},new boolean[] {true},rounding,true));
        expressionLists.add(firstExpressionList);

        // Generate expressions for different numbers of values
        for (int currentNumValues = 2; currentNumValues <= numValues; currentNumValues++) {
            ExpressionList currentExpressionList = new ExpressionList(new ArrayList<>(), currentNumValues, rounding, numTruncators,true);
            int start = currentNumValues - 1;
            int end = currentNumValues / 2;
            if (currentNumValues % 2 == 0) {
                end -= 1;
            }

            for (int i = start; i > end; i--) {
                // Generate combinations of currentNumValues
                List<int[]> combinations = generateCombinations(currentNumValues, i);

                for (int[] combination: combinations) {
                    
                    Set<Integer> combinationSet = new HashSet<>();
                    for (int num : combination) {
                        combinationSet.add(num);
                    }
                    Set<Integer> remainder = new HashSet<>();
                    for (int k = 0; k < currentNumValues; k++) {
                        if (!combinationSet.contains(k)) {
                            remainder.add(k);
                        }
                    }
                    //CountingMain.print((Number[])toArray(combinationSet));
                    //CountingMain.print((Number[])toArray(remainder));

                    ExpressionList newExpressions = ExpressionDynamic.powerSetOfExpressions(
                            expressionLists.get(i - 1).evaluate(toArray(combinationSet)),
                            expressionLists.get(currentNumValues - i - 1).evaluate(toArray(remainder))
                    );

                    for (Expression expression : newExpressions.getExpressions()) {
                        currentExpressionList.add(expression);
                    }
                }
            }
            expressionLists.add(currentExpressionList);
        }

        return expressionLists.get(expressionLists.size() - 1);
    }

    // Helper method to convert Set<Integer> to int[]
       // Helper method to convert Set<Integer> to int[]
       private static int[] toArray(Set<Integer> set) {
        int[] arr = new int[set.size()];
        int index = 0;
        for (int val : set) {
            arr[index++] = val;
        }
        return arr;
    }

    // Static method to combine two expression lists
    public static ExpressionList powerSetOfExpressions(ExpressionList expressionList1, ExpressionList expressionList2) {
        List<Expression> expressions = new ArrayList<>();
        for (int i = 0; i < expressionList1.size(); i++) {
            for (int j = 0; j < expressionList2.size(); j++) {
                Expression expression1 = expressionList1.get(i);
                Expression expression2 = expressionList2.get(j);
                expressions.addAll(Expression.createCombinedExpressions(expression1, expression2));
            }
        }

        return new ExpressionList(expressions, expressionList1.getNumValues(), expressionList1.rounding, expressionList1.numTruncators,true);
    }

    // Helper method to generate all combinations of size 'i' from a range of numbers [0, n)
    private static List<int[]> generateCombinations(int n, int size) {
        List<int[]> combinations = new ArrayList<>();
        generateCombinationsRecursive(combinations, new int[size], 0, 0, n, size);
        return combinations;
    }

    // Recursive method to generate combinations
    private static void generateCombinationsRecursive(List<int[]> combinations, int[] combination, int index, int start, int n, int size) {
        if (index == size) {
            combinations.add(combination.clone()); // Store a copy of the current combination
            return;
        }
        for (int i = start; i < n; i++) {
            combination[index] = i;
            generateCombinationsRecursive(combinations, combination, index + 1, i + 1, n, size);
        }
    }
}
