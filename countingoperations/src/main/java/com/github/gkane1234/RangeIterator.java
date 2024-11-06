package com.github.gkane1234;

import java.util.Iterator;

/**
    This class is used to iterate through all possible sets of numbers in a given range, written in non-decreasing order.
*/
public class RangeIterator implements Iterator<int[]> {
    private int[] range;
    private int numValues;
    private int[] currentNums;
    private boolean hasNext;

    /**
        Constructor for the RangeIterator class.
        @param range: a <code>int[]</code> representing the range of numbers to iterate through.
        @param numValues: an <code>int</code> representing the number of values in the set.
    */
    public RangeIterator(int[] range, int numValues) {
        this.range= range;
        this.numValues=numValues;
        this.currentNums=new int[numValues];
        this.hasNext=true;

        for (int i = 0; i < numValues-1; i++) {
            this.currentNums[i]=range[0];
        }
        this.currentNums[numValues-1]=range[0]-1; //in order to not skip the first set of numbers
    }
    /**
        @inheritDoc
    */
    @Override
    public boolean hasNext() {
        return hasNext;
    }
    /**
        @inheritDoc
    */
    @Override
    public int[] next() {
        if (this.currentNums[numValues-1]<this.range[1]) {
            this.currentNums[numValues-1]++;
        } else {
            int maxedOut=1;
            while (this.currentNums[numValues-1-maxedOut]==this.range[1]) {
                maxedOut++;
            }
            int newMinValue = ++this.currentNums[numValues-1-maxedOut];
            if (newMinValue==this.range[1]&&(numValues-1-maxedOut==0)) {
                hasNext=false;
            }
            for (int i = 0; i < maxedOut; i++) {
                this.currentNums[numValues-1-i]=newMinValue;
            }

        }

        return this.currentNums;
    }
    
}