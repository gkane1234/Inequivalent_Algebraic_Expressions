package com.github.gkane1234;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;


public class CompressedExpressionList extends ExpressionList {
    protected final int expressionSize;
    protected long[] compressedExpressions;
    protected int numExpressions;
    protected int numValues;

    public CompressedExpressionList(long[] compressedExpressions, int numExpressions, int numValues) {
        super(null, numExpressions, numValues);
        this.compressedExpressions = compressedExpressions;
        this.numExpressions = numExpressions;
        this.numValues = numValues;
        this.expressionSize = ExpressionCompression.REQUIRED_BITS(numValues)[3];
    }
    public CompressedExpressionList(int numValues) {
        this(new long[ExpressionCompression.getCompressedExpressionListSize(getMaximumSize(numValues), ExpressionCompression.REQUIRED_BITS(numValues)[3])], 0, numValues);
    }

    @Override
    public Expression[] getExpressions() {
        return ExpressionCompression.decompressExpressionList(compressedExpressions, numExpressions, numValues, true).getExpressions();
    }

    @Override
    public void forceAdd(Expression expression) {
        ExpressionCompression.setCompressedExpression(compressedExpressions, numExpressions, expressionSize, ExpressionCompression.compressExpression(expression));
        numExpressions++;
    }


    public long[] getCompressedExpressions() {
        return compressedExpressions;
    }   
    @Override
    public Expression get(int index) {
        long compressedExpression = ExpressionCompression.getCompressedExpression(compressedExpressions, index, expressionSize);
        return ExpressionCompression.decompressExpression(compressedExpression, numValues);
    }


    public static CompressedExpressionList loadCompressed(int numValues) throws FileNotFoundException {
        String filename = getCompressedFilename(numValues);
        return loadCompressed(filename, numValues, ExpressionList.getMaximumSize(numValues),true);
    }

    public static CompressedExpressionList loadCompressed(String filename, int numValues, int numExpressions, boolean verbose) throws FileNotFoundException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename),BUFFER_SIZE);
            ObjectInputStream ois = new ObjectInputStream(bis)) {
            long[] compressedExpressions = (long[]) ois.readObject();
            return new CompressedExpressionList(compressedExpressions, numExpressions, numValues);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new FileNotFoundException("File not found: " + filename);
        }
    }

    public static void saveCompressed(CompressedExpressionList compressedExpressionList, boolean verbose) {
        String filename = getCompressedFilename(compressedExpressionList.getNumValues());
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename),BUFFER_SIZE);
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(compressedExpressionList.getCompressedExpressions());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
