package com.github.gkane1234;

import java.util.Iterator;


public class RangeIterator implements Iterator<int[]> {
    private int[] range;
    private int numValues;
    private int[] currentNums;
    private boolean hasNext;


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

    @Override
    public boolean hasNext() {
        return hasNext;
    }

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