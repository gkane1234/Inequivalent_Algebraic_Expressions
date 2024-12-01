package com.github.gkane1234;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;


public class CompressedExpressionSet extends ExpressionSet {
    private final int expressionSize;
    private long[] compressedExpressions;
    private int numExpressions;
    private int numValues;

    public CompressedExpressionSet(long[] compressedExpressions, int numExpressions, int numValues) {
        super(null, numExpressions, numValues);
        this.compressedExpressions = compressedExpressions;
        this.numExpressions = numExpressions;
        this.numValues = numValues;
        this.expressionSize = ExpressionCompression.REQUIRED_BITS(numValues)[3];
    }
    @Override
    public Expression[] getExpressions() {
        return ExpressionCompression.decompressExpressionSet(compressedExpressions, numExpressions, numValues, true).getExpressions();
    }

    @Override
    public int getNumExpressions() {
        return numExpressions;
    }

    @Override
    public int getNumValues() {
        return numValues;
    }

    @Override
    public String toString() {
        return Arrays.toString(getExpressions());
    }

    @Override
    public boolean add(Expression expression) {
        throw new UnsupportedOperationException("Cannot add to a compressed expression set");
    }
    @Override
    public void forceAdd(Expression expression) {
        throw new UnsupportedOperationException("Cannot add to a compressed expression set");
    }


    public long[] getCompressedExpressions() {
        return compressedExpressions;
    }   
    @Override
    public Expression get(int index) {
        long compressedExpression = ExpressionCompression.getCompressedExpression(compressedExpressions, index, expressionSize);
        return ExpressionCompression.decompressExpression(compressedExpression, numValues);
    }


    public static CompressedExpressionSet loadCompressed(int numValues) throws FileNotFoundException {
        String filename = getCompressedFilename(numValues);
        return loadCompressed(filename, numValues, ExpressionSet.getMaximumSize(numValues),true);
    }

    public static CompressedExpressionSet loadCompressed(String filename, int numValues, int numExpressions, boolean verbose) throws FileNotFoundException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename),BUFFER_SIZE);
            ObjectInputStream ois = new ObjectInputStream(bis)) {
            long[] compressedExpressions = (long[]) ois.readObject();
            return new CompressedExpressionSet(compressedExpressions, numExpressions, numValues);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new FileNotFoundException("File not found: " + filename);
        }
    }

    public static void saveCompressed(CompressedExpressionSet compressedExpressionSet, boolean verbose) {
        String filename = getCompressedFilename(compressedExpressionSet.getNumValues());
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename),BUFFER_SIZE);
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(compressedExpressionSet.getCompressedExpressions());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
