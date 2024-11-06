package com.github.gkane1234;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.iterator.TByteIterator;
import gnu.trove.set.hash.TByteHashSet;
/*
    A class that creates all inequivalent expressions with a given number of values and operations,
    using a dynamic programming approach.
*/
public class ExpressionDynamic {
    
    private int rounding;
    private int numValues;
    private int numTruncators;
    private boolean verbose;
    /**
        Initializes an ExpressionDynamic class with a given number of values and rounding.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param rounding: an <code>int</code> representing the number of decimal places to round the result to.
        @param numTruncators: an <code>int</code> representing the number of truncators to use. (Defined in ExpressionSet)
        @param ops: an <code>Operation[]</code> representing the operations to use. (Defined in Operation)
        @param verbose: a <code>boolean</code> representing whether to print verbose output.
    */
    public ExpressionDynamic( int numValues, int rounding, int numTruncators,Operation[] ops, boolean verbose) {
        

        if (ops != null) {
            Operation.changeOperations(ops);
        }
        this.numValues = numValues;
        this.rounding = rounding;
        this.numTruncators = numTruncators;
        this.verbose = verbose;
    }


    public ExpressionDynamic() {
        this( 4,5, 3,null, false);
    }

    /**
        Generates all inequivalent expressions with a given number of values and operations by finding all that can be made with 1 value,
        then iteratively finding all that can be made with 2, 3, etc. values, until the full set is found.

        It finds the expressions for the next number by combining the expression sets already found with new value_orders created by permutations,
        and all unique ways to split the next number into two groups without considering order.

        For example, to find the answer for 4 values we combine expressions sets with new values_orders like this:
        {0} with {1,2,3}, {1} with {0,2,3}, {2} with {0,1,3}, {3} with {0,1,2} then
        {0,1} with {2,3}, {0,2} with {1,3}, {0,3} with {1,2}

        Then we add all of these expressions to the expression set, which removed duplicates using expressionSet.add()
        @return an <code>ExpressionSet</code> representing the expression set.
    */
    public ExpressionSet getExpressionSet() {
        
        // Initialize expression lists
        int counter = 0;
        List<ExpressionSet> expressionLists = new ArrayList<>();
        ExpressionSet firstExpressionSet = new ExpressionSet(1, rounding, numTruncators);
        firstExpressionSet.forceAdd(new Expression(new byte[]{0},new byte[]{},new boolean[] {true})); //Base case
        expressionLists.add(firstExpressionSet);


        for (int currentNumValues = 2; currentNumValues <= numValues; currentNumValues++) {
            if (verbose) {
                System.out.println("Generating expressions for "+currentNumValues+" values");
                System.out.println("Number of expressions: "+String.format("%,d",expressionLists.get(currentNumValues-2).getNumExpressions()));
            }
            ExpressionSet currentExpressionSet = new ExpressionSet(currentNumValues, rounding, numTruncators);
            //currentExpressionSet.startAdding();
            int start = (currentNumValues-1);
            int end = (currentNumValues>>1); //since the two groups commute, we only need to consider half of the combinations
            if (currentNumValues % 2 == 0) {
                end -= 1; //off by one error for even numbers
            }

            for (int i = start; i > end; i--) {
                if (verbose) {
                    System.out.println("Generating combinations for "+currentNumValues+" values, group size of "+i);
                    counter=0;
                }
                TByteHashSet[] combinations = generateCombinations(currentNumValues, i);

                for (TByteHashSet combination: combinations) {
                    TByteHashSet remainder = new TByteHashSet();
                    for (byte k = 0; k < currentNumValues; k++) {
                        //add all numbers not in the combination to the remainder (for example {1,2,5} and {0,3,4})
                        if (!combination.contains(k)) {
                            remainder.add(k);
                        }
                    }
                    Expression[] newExpressions = ExpressionDynamic.productOfExpressionSets(
                            expressionLists.get(i - 1).changeValueOrders(toArray(combination)),
                            expressionLists.get(currentNumValues - i - 1).changeValueOrders(toArray(remainder))
                    );

                    for (Expression expression : newExpressions) {
                        boolean added = currentExpressionSet.add(expression);
                        if (verbose && added) {
                            counter++;
                            if (counter  % 100000 == 0) {
                                System.out.println("Added "+counter+" expressions");
                            }
                        }
                    }
                }
            }

            //currentExpressionSet.doneAdding();
            System.out.println("Done generating combinations for "+currentNumValues+" values");
            expressionLists.add(currentExpressionSet);
        }

        return expressionLists.get(expressionLists.size() - 1);
    }
    /**
        Converts a TByteHashSet to a byte array.
        @param set: a <code>TByteHashSet</code> representing the set to convert.
        @return a <code>byte[]</code> representing the converted set.
    */
    private static byte[] toArray(TByteHashSet set) {
        
        byte[] arr = new byte[set.size()];
        byte index = 0;
        TByteIterator vals = set.iterator();
        while (vals.hasNext()){
            arr[index++] = vals.next();
        }
        return arr;
    }
    /**
        Combines two expression sets by constructing a mathematical product of the two sets and all possible operations including non-commutative reordering.

        The redundancy is removed in ExpressionSet.add() later.
        @param expressionSet1: an <code>ExpressionSet</code> representing the first set to combine.
        @param expressionSet2: an <code>ExpressionSet</code> representing the second set to combine.
        @return an <code>Expression[]</code> representing the combined set.
    */
    public static Expression[] productOfExpressionSets(ExpressionSet expressionSet1, ExpressionSet expressionSet2) {
        
        int newValues = expressionSet1.getNumExpressions()*expressionSet2.getNumExpressions()*Operation.getNumOperationOrderings();
        Expression[] expressions = new Expression[newValues];
        for (int i = 0; i < expressionSet1.getNumExpressions(); i++) {
            for (int j = 0; j < expressionSet2.getNumExpressions(); j++) {
                Expression expression1 = expressionSet1.get(i);
                Expression expression2 = expressionSet2.get(j);
                Expression[] combinedExpressions = Expression.createCombinedExpressions(expression1, expression2);
                for (int k = 0; k< combinedExpressions.length; k++) {
                    expressions[(i*expressionSet2.getNumExpressions()+j)*6+k] = combinedExpressions[k];
                }
            }
        }
        return expressions;
    }
    /**
        Generates all combinations of length 'size' from a range of numbers [0, n).
        @param n: an <code>int</code> representing the range of numbers to generate combinations from.
        @param size: an <code>int</code> representing the length of the combinations to generate.
        @return an <code>TByteHashSet[]</code> representing the combinations.
    */
    private static TByteHashSet[] generateCombinations(int n, int size) {
        
        TByteHashSet[] combinations = new TByteHashSet[binomialCoefficient(n, size)];
        int[] combinationIndex = {0}; // Use array to track current index, as it is mutable
        generateCombinationsRecursive(combinations, new TByteHashSet(size), 0, 0, n, size, combinationIndex);
        return combinations;
    }
    /**
        Recursively generates combinations.
        @param combinations: an <code>TByteHashSet[]</code> representing the combinations to generate.
        @param combination: a <code>TByteHashSet</code> representing the current combination being generated.
        @param index: an <code>int</code> representing the current index in the combination.
        @param start: an <code>int</code> representing the starting number to generate combinations from.
        @param n: an <code>int</code> representing the range of numbers to generate combinations from.
        @param size: an <code>int</code> representing the length of the combinations to generate.
        @param combinationIndex: an <code>int[]</code> representing the current index in the combinations array.
    */
    private static void generateCombinationsRecursive(TByteHashSet[] combinations, TByteHashSet combination, int index, int start, int n, int size, int[] combinationIndex) {
        
        if (index == size) {
            combinations[combinationIndex[0]++] = new TByteHashSet(combination); // Store a copy and increment index
            return;
        }
        for (int i = start; i < n; i++) {
            combination.add((byte)i);
            generateCombinationsRecursive(combinations, combination, index + 1, i + 1, n, size, combinationIndex);
            combination.remove((byte)i); // Remove element for backtracking
        }
    }
    /**
        Calculates the binomial coefficient.
        @param n: an <code>int</code> representing the total number of items.
        @param k: an <code>int</code> representing the number of items to choose.
        @return an <code>int</code> representing the binomial coefficient.
    */
    private static int binomialCoefficient(int n, int k) {
        return (int) (factorial(n) / (factorial(k) * factorial(n - k)));
    }
    /**
        Calculates the factorial of a number.
        @param n: an <code>int</code> representing the number to calculate the factorial of.
        @return an <code>int</code> representing the factorial of the number.
    */
    private static int factorial(int n) {
        int result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}
