package com.github.gkane1234;

import java.util.Iterator;
import java.util.List;


public class RangeIterator implements Iterator<List<Integer>> {
    int[] range;
    int numValues;

    int currentNum;


    public RangeIterator(int[] range, int numValues) {
        this.range= range;
        this.numValues=numValues;
        this.currentNum=range[0];


    }

    @Override
    public boolean hasNext() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasNext'");
    }

    @Override
    public List<Integer> next() {

        
        
    }
    
}