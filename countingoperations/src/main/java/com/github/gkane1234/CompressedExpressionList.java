package com.github.gkane1234;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/**
    A class that stores a list of compressed expressions.
*/
public class CompressedExpressionList extends ExpressionList {
    protected final int expressionSize;
    protected long[] compressedExpressions;
    protected int numExpressions;
    protected int numValues;

    /**
        Constructor for a CompressedExpressionList.
        @param compressedExpressions: the compressed expressions to use.
        @param numExpressions: an <code>int</code> representing the number of expressions that are in expressions. (This can differ from the length of expressions)
        @param numValues: an <code>int</code> representing the number of values in the expressions.
    */
    public CompressedExpressionList(long[] compressedExpressions, int numExpressions, int numValues) {
        super(null, numExpressions, numValues); // null is used because we're using compressed expressions
        this.compressedExpressions = compressedExpressions;
        this.numExpressions = numExpressions;
        this.numValues = numValues;
        this.expressionSize = ExpressionCompression.REQUIRED_BITS(numValues)[3];
    }
    /**
        Constructor for a CompressedExpressionList.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
    */
    public CompressedExpressionList(int numValues) {
        this(new long[ExpressionCompression.getCompressedExpressionListSize(getMaximumSize(numValues), ExpressionCompression.REQUIRED_BITS(numValues)[3])], 0, numValues);
    }
    /**
        Constructor for a CompressedExpressionList.
        @param expressionList: an <code>ExpressionList</code> to compress.
    */
    public CompressedExpressionList(ExpressionList expressionList) {
        this(ExpressionCompression.compressExpressionList(expressionList));
    }
    /**
        Constructor for a CompressedExpressionList.
        @param compressedExpressionList: a <code>CompressedExpressionList</code> to copy.
    */
    public CompressedExpressionList(CompressedExpressionList compressedExpressionList) {
        this(compressedExpressionList.getCompressedExpressions(), compressedExpressionList.getNumExpressions(), compressedExpressionList.getNumValues());
    }

    @Override
    /**
        Gets the expressions from the compressed expression list.
        This decompresses the expressions.
        @return an <code>Expression[]</code> representing the expressions.
    */
    public Expression[] getExpressions() {
        return ExpressionCompression.decompressExpressionList(compressedExpressions, numExpressions, numValues, true).getExpressions();
    }

    @Override
    /**
        Adds an expression to the compressed expression list.
        This compresses the expression.
    */
    public void forceAdd(Expression expression) {
        ExpressionCompression.setCompressedExpression(compressedExpressions, numExpressions, expressionSize, ExpressionCompression.compressExpression(expression));
        numExpressions++;
    }

    /**
        Gets the compressed expressions from the compressed expression list.
        @return a <code>long[]</code> representing the compressed expressions.
    */
    public long[] getCompressedExpressions() {
        return compressedExpressions;
    }   

    /**
        Decompresses the compressed expression list.
        @return an <code>ExpressionList</code> representing the decompressed expressions.
    */
    public ExpressionList decompress() {
        return ExpressionCompression.decompressExpressionList(compressedExpressions, numExpressions, numValues, true);
    }

    @Override
    /**
        Gets an expression from the compressed expression list.
        @param index: an <code>int</code> representing the index of the expression to get.
        @return an <code>Expression</code> representing the expression.
    */
    public Expression get(int index) {
        long compressedExpression = ExpressionCompression.getCompressedExpression(compressedExpressions, index, expressionSize);
        return ExpressionCompression.decompressExpression(compressedExpression, numValues);
    }


    /**
        Loads a compressed expression list from a file.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @return a <code>CompressedExpressionList</code> representing the compressed expression list.
        @throws FileNotFoundException if the file is not found.
    */
    public static CompressedExpressionList loadCompressed(int numValues) throws FileNotFoundException {
        String filename = getCompressedFilename(numValues);
        return loadCompressed(filename, numValues, ExpressionList.getMaximumSize(numValues),true);
    }

    /**
        Loads a compressed expression list from a file.
        @param filename: a <code>String</code> representing the filename of the compressed expression list.
        @param numValues: an <code>int</code> representing the number of values in the expressions.
        @param numExpressions: an <code>int</code> representing the number of expressions that are in the compressed expression list.
        @param verbose: a <code>boolean</code> representing whether to print verbose output.
        @return a <code>CompressedExpressionList</code> representing the compressed expression list.
        @throws FileNotFoundException if the file is not found.
    */
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
    /**
        Saves a compressed expression list to a file.
        @param compressedExpressionList: a <code>CompressedExpressionList</code> to save.
        @param verbose: a <code>boolean</code> representing whether to print verbose output.
    */
    public static void saveCompressed(CompressedExpressionList compressedExpressionList, boolean verbose) {
        String filename = getCompressedFilename(compressedExpressionList.getNumValues());
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename),BUFFER_SIZE);
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(compressedExpressionList.getCompressedExpressions());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
        Creates a new CompressedExpressionList with a new value order.
        @param compressedExpressionList: a <code>CompressedExpressionList</code> to create a new list from.
        @param value_order: a <code>byte[]</code> representing the new value order.
        @param numTruncators: the number of truncators to use.
        @return an <code>CompressedExpressionList</code> representing the new list.
    */
    public static CompressedExpressionList changeValueOrder(CompressedExpressionList compressedExpressionList, byte[] valueOrder, int numTruncators) throws IllegalStateException {
        
        Expression[] newExpressions = new Expression[compressedExpressionList.getNumExpressions()];
        for (int i=0;i<compressedExpressionList.getNumExpressions();i++) {
            newExpressions[i]=compressedExpressionList.get(i).changeValueOrder(valueOrder);
        }

        CompressedExpressionList compressedExpressions = ExpressionCompression.compressExpressionList(new ExpressionList(newExpressions, newExpressions.length, compressedExpressionList.getNumValues()));


        return compressedExpressions;
    }

    
}
