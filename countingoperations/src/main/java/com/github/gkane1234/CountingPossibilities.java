package com.github.gkane1234;

import java.util.HashMap;

public class CountingPossibilities {
    public static HashMap<int[],Integer> seen;
    /**
        Counts the number of distinct sets of numbers where the order does not matter.
        @param numNumbers: an <code>int</code> representing the number of numbers in the set.
        @param numPossibleValues: an <code>int</code> representing the number of possible values for each number.
        @return an <code>int</code> representing the number of distinct sets of numbers where the order does not matter.
    */
    public static int numberOfDistinctSetsOfNumbersWhereOrderDoesNotMatter(int numNumbers,int numPossibleValues) {
        seen = new HashMap<>();
        return recursiveNumberOfDistinctSetsOfNumbersWhereOrderDoesNotMatter(numNumbers, numPossibleValues);
        
    }
    /**
        Recursively counts the number of distinct sets of numbers where the order does not matter.
        @param numNumbers: an <code>int</code> representing the number of numbers in the set.
        @param numPossibleValues: an <code>int</code> representing the number of possible values for each number.
        @return an <code>int</code> representing the number of distinct sets of numbers where the order does not matter.
    */
    private static int recursiveNumberOfDistinctSetsOfNumbersWhereOrderDoesNotMatter(int numNumbers,int numPossibleValues) {
        if (numNumbers==1) {
            return numPossibleValues;
        }
        int answer = 0;
        for (int i = 0; i <= numPossibleValues; i++) {
            int[] nextEntry = new int[]{numNumbers-1,i};
            if (seen.containsKey(nextEntry)){
                answer+=seen.get(nextEntry);
            }
            else {
                int newValue = numberOfDistinctSetsOfNumbersWhereOrderDoesNotMatter(numNumbers-1, i);
                answer+=newValue;
                seen.put(nextEntry,newValue);
            }
            
        }
        return answer;
    }
}