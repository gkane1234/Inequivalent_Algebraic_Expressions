package com.github.gkane1234;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.iterator.TByteIterator;
import gnu.trove.set.hash.TByteHashSet;
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
        ExpressionList firstExpressionList = new ExpressionList(1, rounding, numTruncators,true);
        firstExpressionList.add(new Expression(new byte[]{0},new byte[]{},new boolean[] {true}));
        expressionLists.add(firstExpressionList);


        // Generate expressions for different numbers of values
        for (int currentNumValues = 2; currentNumValues <= numValues; currentNumValues++) {
            ExpressionList currentExpressionList = new ExpressionList(currentNumValues, rounding, numTruncators,true);
            int start = (currentNumValues-1);
            int end = (currentNumValues>>1);
            if (currentNumValues % 2 == 0) {
                end -= 1;
            }

            for (int i = start; i > end; i--) {
                // Generate combinations of currentNumValues
                List<byte[]> combinations = generateCombinations(currentNumValues, i);

                for (byte[] combination: combinations) {
                    
                    TByteHashSet combinationSet = new TByteHashSet();
                    for (byte num : combination) {
                        combinationSet.add(num);
                    }
                    TByteHashSet remainder = new TByteHashSet();
                    for (byte k = 0; k < currentNumValues; k++) {
                        if (!combinationSet.contains(k)) {
                            remainder.add(k);
                        }
                    }
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
       private static byte[] toArray(TByteHashSet set) {
        byte[] arr = new byte[set.size()];
        byte index = 0;
        TByteIterator vals = set.iterator();
        while (vals.hasNext()){
            arr[index++] = vals.next();
        }
        return arr;
    }

    // Static method to combine two expression lists
    public static ExpressionList powerSetOfExpressions(ExpressionList expressionList1, ExpressionList expressionList2) {
        int newValues = expressionList1.getNumExpressions()*expressionList2.getNumExpressions()*6;
        Expression[] expressions = new Expression[newValues];
        for (int i = 0; i < expressionList1.getNumExpressions(); i++) {
            for (int j = 0; j < expressionList2.getNumExpressions(); j++) {
                Expression expression1 = expressionList1.get(i);
                Expression expression2 = expressionList2.get(j);
                List<Expression> combinedExpressions = Expression.createCombinedExpressions(expression1, expression2);
                for (int k = 0; k< combinedExpressions.size(); k++) {
                    expressions[(i*expressionList2.getNumExpressions()+j)*6+k] = combinedExpressions.get(k);
                }
            }
        }
        return new ExpressionList(expressions, newValues, expressionList1.getNumExpressions()+expressionList2.getNumExpressions(),expressionList1.rounding, expressionList1.numTruncators,true);
    }

    // Helper method to generate all combinations of size 'i' from a range of numbers [0, n)
    private static List<byte[]> generateCombinations(int n, int size) {
        List<byte[]> combinations = new ArrayList<>();
        generateCombinationsRecursive(combinations, new byte[size], 0, 0, n, size);
        return combinations;
    }

    // Recursive method to generate combinations
    private static void generateCombinationsRecursive(List<byte[]> combinations, byte[] combination, int index, int start, int n, int size) {
        if (index == size) {
            combinations.add(combination.clone()); // Store a copy of the current combination
            return;
        }
        for (int i = start; i < n; i++) {
            combination[index] = (byte)i;
            generateCombinationsRecursive(combinations, combination,(index+1),(i+1), n, size);
        }
    }
}
