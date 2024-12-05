package com.github.gkane1234;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


/**
    This class is used to compress and decompress expressions and expression lists.
*/
public class ExpressionCompression {

    private ExpressionCompression() {
        throw new UnsupportedOperationException("This class is not meant to be instantiated.");
    }
    /**
        Calculates the required bits for the parts of an expression to be compressed.
        @param numValues: an <code>int</code> representing the number of values in the expression.
        @return an <code>int[]</code> representing the required bits for the value order, operations, and order.
    */
    public static int[] REQUIRED_BITS(int numValues) {
        int requiredBitsValueOrder = getRequiredBitsValueOrder(numValues);
        int requiredBitsOperations = getRequiredBitsOperations(numValues);
        int requiredBitsOrder = getRequiredBitsOrder(numValues);
        int requiredBitsTotal = Math.max(requiredBitsValueOrder+requiredBitsOperations+requiredBitsOrder, 1);
        return new int[]{requiredBitsValueOrder,
                        requiredBitsOperations,
                        requiredBitsOrder,
                        requiredBitsTotal};
    }
    /**
        Calculates the required bits for the order of an expression to be compressed.
        @param numValues: an <code>int</code> representing the number of values in the expression.
        @return an <code>int</code> representing the required bits for the order.
    */
    public static int getRequiredBitsOrder(int numValues) {
        return Math.max(2*(numValues)-5, 0);
    }
    /**
        Calculates the required bits for the value order of an expression to be compressed.
        The value order is saved as a permutation number which represents a specific permutation of the values, hence the factorial.
        @param numValues: an <code>int</code> representing the number of values in the expression.
        @return an <code>int</code> representing the required bits for the value order.
    */
    public static int getRequiredBitsValueOrder(int numValues) {
        return (int) (Math.floor(Math.log(factorial(numValues))/Math.log(2)));
    }
    /**
        Calculates the required bits for the operations of an expression to be compressed.
        @param numValues: an <code>int</code> representing the number of values in the expression.
        @return an <code>int</code> representing the required bits for the operations.
    */
    public static int getRequiredBitsOperations(int numValues) {
        return 2*(numValues)-2;
    }
    /**
        Compresses an expression list into a long array.
        This is done by compressing each expression in the list and then storing the compressed expressions in a long array.
        They are added back to back in the long array using bitwise operations, dealing appropraitely with overflow 
        The length of the compressed expression is generally not a factor of 64 but is strictly less than 64.
        @param toCompress: an <code>ExpressionList</code> to compress.
        @return a <code>CompressedExpressionList</code> representing the compressed expression list.
    */
    public static CompressedExpressionList compressExpressionList (ExpressionList toCompress) {

        if (toCompress.numValues==1) {
            return new CompressedExpressionList(new long[]{0}, 1, 1);
        }
        int numValues = toCompress.getNumValues();
        int requiredBitsTotal= REQUIRED_BITS(numValues)[3];

        long[] compressedExpressions = new long[getCompressedExpressionListSize(toCompress.getNumExpressions(), requiredBitsTotal)];

        int currentLong = 0;
        int currentBit = 0;

        
        for (int i = 0; i < toCompress.getNumExpressions(); i++) {
            long compressedExpression = compressExpression(toCompress.get(i));
            compressedExpressions[currentLong] |= (compressedExpression << currentBit);
            if (64<requiredBitsTotal+currentBit) {
                currentBit = requiredBitsTotal + currentBit - 64;
                currentLong++;
                compressedExpressions[currentLong] = compressedExpression >>> (requiredBitsTotal-currentBit);
            }
            else{
                currentBit += requiredBitsTotal;
                if (currentBit==64) {
                    currentBit=0;
                    currentLong++;
                }
            }
        }
        return new CompressedExpressionList(compressedExpressions, toCompress.getNumExpressions(), numValues);
        
    }
    public static String toBinary(long value) {
        return String.format("%64s", Long.toBinaryString(value)).replace(' ', '0');
    }
    /**
        Decompresses a compressed expression list into an expression list.
        @param compressedExpressionList: a <code>CompressedExpressionList</code> to decompress.
        @param verbose: a <code>boolean</code> representing whether to print verbose output.
        @return an <code>ExpressionList</code> representing the decompressed expression list.
    */
    public static ExpressionList decompressExpressionList (CompressedExpressionList compressedExpressionList, boolean verbose) {
        return decompressExpressionList(compressedExpressionList.getCompressedExpressions(), compressedExpressionList.getNumExpressions(), compressedExpressionList.getNumValues(), verbose);
    }
    /**
        Decompresses a compressed expression list into an expression list.
        @param compressedExpressions: a <code>long[]</code> representing the compressed expression list.
        @param numCompressedExpressions: an <code>int</code> representing the number of expressions in the compressed expression list.
        @param numValues: an <code>int</code> representing the number of values in the expression.
        @param verbose: a <code>boolean</code> representing whether to print verbose output.
        @return an <code>ExpressionList</code> representing the decompressed expression list.
    */
    public static ExpressionList decompressExpressionList (long[] compressedExpressions, int numCompressedExpressions, int numValues, boolean verbose) {
        long startTime = System.currentTimeMillis();


        if (verbose) {

            System.err.println("Decompressing expression set with "+numCompressedExpressions+" expressions");
        }
        // the n=1 case is handled separately.
        if (numValues==1) {
            return new ExpressionList(new Expression[]{decompressExpression(compressedExpressions[0], numValues)}, 1, numValues);
        }
        
        int[] requiredBits= REQUIRED_BITS(numValues);
        //used for verbose output
        int decile = 0;
        if (verbose) {
            decile = Math.max(1, numCompressedExpressions/10); // needs to be at least 1 or we divide by 0
        }

        List<Expression> decompressedExpressions = new ArrayList<>();
        
        for (int i=0; i<numCompressedExpressions; i++) {
            if (verbose && i%decile==0) {
                System.err.println("Decompressed "+i+"/"+numCompressedExpressions+" expressions");
            }
            long currentCompressedExpression = getCompressedExpression(compressedExpressions, i, requiredBits[3]);
            decompressedExpressions.add(decompressExpression(currentCompressedExpression, numValues));
        }
        Expression[] decompressedExpressionsArray = new Expression[decompressedExpressions.size()];
        long endTime = System.currentTimeMillis();
        if (verbose) {
            System.out.println("Decompressed expression set in "+(endTime-startTime)/1000.0+" seconds");
        }
        return new ExpressionList(decompressedExpressions.toArray(decompressedExpressionsArray),numCompressedExpressions, numValues);

            



    }
    /**
        Gets a compressed expression from a compressed expression list.
        @param compressedExpressions: a <code>long[]</code> representing the compressed expression list.
        @param index: an <code>int</code> representing the index of the compressed expression to get.
        @param expressionSize: an <code>int</code> representing the size of the expression in bits.
        @return a <code>long</code> representing the compressed expression.
    */
    public static long getCompressedExpression(long[] compressedExpressions, int index, int expressionSize) {
        int bitIndex = index*expressionSize;
        int longIndex = bitIndex/64;
        int bitOffset = bitIndex%64;
        long compressedExpression = 0;
        compressedExpression |= (compressedExpressions[longIndex] >>> bitOffset);
        if (64<expressionSize+bitOffset) {
            int bitsUsed = 64-bitOffset;
            bitOffset = expressionSize+bitOffset-64;
            compressedExpression |= ((compressedExpressions[longIndex+1] & ((1L<<bitOffset)-1)) << bitsUsed);
        } else {
            compressedExpression &= ((1L<<expressionSize)-1);
        }
        return compressedExpression;
    }
    /**
        Sets a compressed expression in a compressed expression list.
        @param compressedExpressions: a <code>long[]</code> representing the compressed expression list.
        @param index: an <code>int</code> representing the index of the compressed expression to set.
        @param expressionSize: an <code>int</code> representing the size of the expression in bits.
        @param value: a <code>long</code> representing the value to set.
    */
    public static void setCompressedExpression(long[] compressedExpressions, int index, int expressionSize, long value) {
        int bitIndex = index*expressionSize;
        int longIndex = bitIndex/64;
        int bitOffset = bitIndex%64;
        //System.err.println("Setting compressed expression at index "+index+" with value "+toBinary(value)+" and bit offset "+bitOffset+" and long index "+longIndex);
        compressedExpressions[longIndex] |= (value << bitOffset);
        if (64<expressionSize+bitOffset) {
            int bitsUsed = 64-bitOffset;
            bitOffset = expressionSize+bitOffset-64;
            compressedExpressions[longIndex+1] |= (value >>> bitsUsed);
        }
    }
    /**
        Gets the size that the long array representing the expression list must be to store all the expressions.
        @param numExpressions: an <code>int</code> representing the number of expressions in the compressed expression list.
        @param expressionSize: an <code>int</code> representing the size of the expression in bits.
        @return an <code>int</code> representing the size of the compressed expression list.
    */
    public static int getCompressedExpressionListSize(int numExpressions, int expressionSize) {
        // we add 63 to the number of bits to include an extra long if the number of bits is not a factor of 64
        return (numExpressions*expressionSize+63)/64;
    }

    /**
     * Compresses an expression into a long.
     * The compressed expression is a single long that stores the value order, operations, and order
     * in that order in ascending bit significance.
     * @param toCompress: an <code>Expression</code> to compress
     * @return a <code>long</code> representing the compressed expression
     * 
     * for example:
     * valueOrder = [0,2,1,3] 
     * operations = [1,1,1] 
     * order = [0,0,1,0,1,0,1]
     * 
     * --> valueOrder
     * 0,2,1,3 --> {0,2,0} --> 0*(1!)+2*(2!)+0*(3!) --> 4=00100b
     * 
     * --> operations
     * 1,1,1 --> 010101b
     * 
     * --> order
     * 0,0,1,0,1,0,1 --> 101b
     * 
     * all together:
     * 101 010101 00100 or 2724 in decimal
     * 
     * Approx bits required:
     *          val     ops     order       required bits
     * n=1 --> 1 bit    0        0          1
     * n=2 --> 1 bit    2 bits   0 bit      3
     * n=3 --> 3 bits   4 bits   2 bits     9
     * n=4 --> 5 bits   6 bits   4 bits     15
     * n=5 --> 7 bits   8 bits   6 bits     21
     * n=6 --> 9 bits   10 bits  8 bits     27
     * n=7 --> 12 bits  12 bits  10 bits    34
     * n=8 --> 15 bits  14 bits  12 bits    41
     * n=9 --> 18 bits  16 bits  14 bits    48
     * n=10--> 21 bits  18 bits  16 bits    55
     *
     */
    public static long compressExpression (Expression toCompress) {

        int numValues = toCompress.valueOrder.length;

        if (numValues==1) {
            return 0L;
        }
        
        int[] requiredBits= REQUIRED_BITS(numValues);
        long compressedValueOrder = valueOrderToLong(toCompress.valueOrder);
        long compressedOperations = operationsToLong(toCompress.operations);
        long compressedOrder = orderToLong(toCompress.order);
        
        long compressedExpression = 
            (compressedValueOrder & ((1L << requiredBits[0]) - 1)) |  
            ((compressedOperations << requiredBits[0]) | 
            ((compressedOrder << (requiredBits[0] + requiredBits[1]))));
        return compressedExpression;
    }
    /**
        Decompresses a compressed expression into an expression.
        @param compressedExpression: a <code>long</code> representing the compressed expression.
        @param numValues: an <code>int</code> representing the number of values in the expression.
        @return an <code>Expression</code> representing the decompressed expression.

        For more information on the compression scheme, see the compressExpression method.
    */
    public static Expression decompressExpression (long compressedExpression, int numValues) {
        if (numValues==1) {
            return new Expression(new byte[]{0},new byte[]{},new boolean[] {true});
        }
        int[] requiredBits= REQUIRED_BITS(numValues);
        byte[] valueOrder = longToValueOrder(compressedExpression & ((1L<<requiredBits[0])-1), numValues);
        byte[] operations = longToOperations((compressedExpression >>> requiredBits[0] & ((1L<<requiredBits[1])-1)), numValues-1);
        boolean[] order = longToOrder((compressedExpression >>> (requiredBits[0]+requiredBits[1]) & ((1L<<requiredBits[2])-1)), getRequiredBitsOrder(numValues));
        return new Expression(valueOrder, operations, order);

    }

    /**
        Converts an order array into an integer representation.
        Up to 4 values are removed, two from the start and two from the end,
        since the first two values and last value of order must be true true false in order to make a valid RPN expression.
        The penultimate value is determined since there is always the same number of true and falses in the middle.
        @param order: a <code>boolean[]</code> representing the order.
        @return a <code>long</code> representing the order.
    */
    public static long orderToLong(boolean[] order) {
        long value = 0;
        for (int i = 2; i < order.length-2; i++) { 
            value |= (order[i] ? 1 : 0) << (i-2);
        }
        return value;
    }
    /**
        Converts an integer representation into the corresponding order array.
        @param value: a <code>long</code> representing the order.
        @param length: an <code>int</code> representing the length of the order.
        @return a <code>boolean[]</code> representing the order.
    */
    public static boolean[] longToOrder(long value, int length) { 
        //handle the n=2 case
        if (length==0) {
            return new boolean[]{true,true,false};
        }
        boolean[] order = new boolean[length+4];
        order[0]= true; 
        order[1]= true;
        order[length+3]= false;
        int numberOfTrues = 0;
        for (int i = 0; i < length; i++) {
            order[i+2] = ((value >> i) & 1) == 1;
            if (order[i+2]) {
                numberOfTrues++;
            }
        }
        order[length+2] = numberOfTrues!=(length+1)/2;
        return order;
    }
    /**
        Converts an operations array into an integer representation.
        Operations can be one of 4 values, so we can use 2 bits to store each operation.
        We can then just convert the byte array to an integer.
        TODO: allow for different numbers of operations by somehow including what the operations are a compressed expression list.
        @param operations: a <code>byte[]</code> representing the operations.
        @return a <code>long</code> representing the operations.
    */
    public static long operationsToLong(byte[] operations) {
        long value = 0;
        for (int i = 0; i < operations.length; i++) {
            value |= (operations[i] << (i*2));
        }
        return value;
    }
    /**
        Converts an integer representation into the corresponding operations array.
        @param value: a <code>long</code> representing the operations.
        @param length: an <code>int</code> representing the length of the operations.
        @return a <code>byte[]</code> representing the operations.
    */
    public static byte[] longToOperations(long value, int length) {
        byte[] operations = new byte[length];
        for (int i = 0; i < length; i++) {
            operations[i] = (byte) (value & 0b11);
            value >>= 2;
        }
        return operations;
    }
    /**
        Converts a value order array into long representation.
        This is done using a permutation to number mapping found at https://stackoverflow.com/questions/1506078/fast-permutation-number-permutation-mapping-algorithms
        The representation is
        @param valueOrder: a <code>byte[]</code> representing the value order.
        @return a <code>long</code> representing the value order.
    */
    public static long valueOrderToLong(byte[] valueOrder) {
        long value = 0;
        int[] simplifiedValueOrder = new int[valueOrder.length-1];
        boolean[] seen = new boolean[valueOrder.length];

        for (int i =0;i<valueOrder.length-1;i++) {
            int offset= 0;
            for (int j = 0; j < valueOrder[i]; j++) {
                if (seen[j]) {
                    offset++;
                }
            }
            simplifiedValueOrder[i]=valueOrder[i]-offset;
            seen[valueOrder[i]]=true;
        }
        for (int i = simplifiedValueOrder.length-1; i >= 0; i--) {
            value += simplifiedValueOrder[i] * factorial(simplifiedValueOrder.length-i);
        }
        return value;
    }
    /**
        Converts a long representation into the corresponding value order array.
        This is done using a permutation to number mapping found at https://stackoverflow.com/questions/1506078/fast-permutation-number-permutation-mapping-algorithms
        @param value: a <code>long</code> representing the value order.
        @param length: an <code>int</code> representing the length of the value order.
        @return a <code>byte[]</code> representing the value order.
    */
    public static byte[] longToValueOrder(long value, int length) {
        
        int[] permutation = new int[length];
        boolean[] seen = new boolean[length];
        int base = 2;

        for (int i = 0; i < length-1; i++) { //calculate the first n-1 values, since n=th is always 0
            
            permutation[length-i-2] = (int) (value % base); 
            value /= base;
            base++;
        }

        byte[] valueOrder = new byte[length];
        for (int i = 0 ; i < length-1; i++) {
            int offset = 0;
            while (permutation[i]>0||seen[offset]) {
                if (!seen[offset]) {
                    permutation[i]--;
                }
                offset++;
            }
            permutation[i] = offset;

            seen[permutation[i]] = true;
            valueOrder[i]=(byte) permutation[i];
        } 
        
        byte i = 0 ; //the last element is the last remaining
        while (seen[i]) {
            i+=1;
        }
        valueOrder[length-1]=i;
        return valueOrder;
    }

    /**
        Calculates the factorial of a number.
        @param n: an <code>int</code> representing the number to calculate the factorial of.
        @return an <code>int</code> representing the factorial of the number.
    */
    private static int factorial(int n) {
        int value = 1;
        for (int i = 2; i <= n; i++) {
            value *= i;
        }
        return value;
    }
}
