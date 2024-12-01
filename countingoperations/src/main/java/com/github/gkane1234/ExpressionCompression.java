package com.github.gkane1234;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;



public class ExpressionCompression {
    public static int[] REQUIRED_BITS(int numValues) {
        //int[] requiredBitsValueOrders = {0,1,3,5,7,9,12,15,18,21};
        int requiredBitsValueOrder = (int) (Math.ceil(Math.log(factorial(numValues))/Math.log(2)));
        int requiredBitsOperations = 2*(numValues)-2;
        int requiredBitsOrder = Math.max(2*(numValues)-4, 0);
        int requiredBitsTotal = requiredBitsValueOrder+requiredBitsOperations+requiredBitsOrder;
        return new int[]{requiredBitsValueOrder,
                        requiredBitsOperations,
                        requiredBitsOrder,
                        requiredBitsTotal};
    }
    public static long[] compressExpressionSet (ExpressionSet toCompress) {

        // Expression is stored as three arrays, two byte arrays and one boolean array
        // The first byte array stores the order of the values
        // the second byte array stores the order and type of operations
        // the third boolean array stores the order of values and operations
        // to compress, we can store a single array of bytes
        // we could also bitmask the operations and store the values as a unique entry in a map to permutations
        /*
         * for example:
         * value_order = [0,2,1,3] 
         * operations = [1,1,1] 
         * order = [0,0,1,0,1,0,1]
         * 
         * --> operations_map
         * 0 --> 00
         * 1 --> 01
         * 2 --> 10
         * 3 --> 11
         * 
         * 
         * 0,2,1,3 --> {0,2,0} --> 0*(1!)+2*(2!)+0*(3!)
         * 
         * operations = 010101
         *          val     ops     order
         * n=1 --> 1 bit    0        0          1
         * n=2 --> 1 bit    2 bits   0 bit      3
         * n=3 --> 3 bits   4 bits   2 bits     9
         * n=4 --> 5 bits   6 bits   4 bits     15
         * n=5 --> 7 bits   8 bits   6 bits     21
         * n=6 --> 9 bits   10 bits  8 bits     27
         * n=7 --> 12 bits  12 bits  10 bits    34
         * n=8 --> 15 bits  14 bits  12 bits    41
         * n=9 --> 18 bits  16 bits  14 bits    48
         * n=10 -->21 bits  18 bits  16 bits    55
         * 
         * 
         * 
         */
        int numValues = toCompress.getNumValues();
        int[] requiredBits= REQUIRED_BITS(numValues);
        long[] compressedExpressions = new long[(toCompress.getNumExpressions()*requiredBits[3]+63)/64];

        int currentLong = 0;
        int currentBit = 0;

        
        for (int i = 0; i < toCompress.getNumExpressions(); i++) {
            long compressedExpression = compressExpression(toCompress.get(i));
            compressedExpressions[currentLong] |= (compressedExpression << currentBit);
            if (64<requiredBits[3]+currentBit) {
                currentBit = requiredBits[3] + currentBit - 64;
                currentLong++;
                compressedExpressions[currentLong] = compressedExpression >>> (requiredBits[3]-currentBit);
            }
            else{
                currentBit += requiredBits[3];
                if (currentBit==64) {
                    currentBit=0;
                    currentLong++;
                }
            }
        }
        return compressedExpressions;
        
    }
    public static String toBinary(long value) {
        return String.format("%64s", Long.toBinaryString(value)).replace(' ', '0');
    }
    public static ExpressionSet decompressExpressionSet (long[] compressedExpressions, int numCompressedExpressions, int numValues, boolean verbose) {
        long startTime = System.currentTimeMillis();
        if (verbose) {

            System.err.println("Decompressing expression set with "+numCompressedExpressions+" expressions");
        }
        
        int[] requiredBits= REQUIRED_BITS(numValues);
        int decile = numCompressedExpressions/10;

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
        System.out.println("Decompressed expression set in "+(endTime-startTime)/1000.0+" seconds");
        return new ExpressionSet(decompressedExpressions.toArray(decompressedExpressionsArray),numCompressedExpressions, numValues);

            



    }
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
     * Compresses an expression into a long.
     * The compressed expression is a single long that stores the value order, operations, and order
     * in that order in ascending bit significance.
     * @param toCompress: an <code>Expression</code> to compress
     * @return a <code>long</code> representing the compressed expression
    
     */
    public static long compressExpression (Expression toCompress) {
        int numValues = toCompress.valueOrder.length;
        int[] requiredBits= REQUIRED_BITS(numValues);
        int compressedValueOrder = valueOrderToInt(toCompress.valueOrder);
        int compressedOperations = operationsToInt(toCompress.operations);
        int compressedOrder = orderToInt(toCompress.order);
        //System.err.println("Original value order: "+Arrays.toString(toCompress.valueOrder));
        //System.err.println("Original operations: "+Arrays.toString(toCompress.operations));
        //System.err.println("Original order: "+Arrays.toString(toCompress.order));
        //System.err.println("Compressed value order: "+toBinary(compressedValueOrder));
        //System.err.println("Compressed operations: "+toBinary(compressedOperations));
        //System.err.println("Compressed order: "+toBinary(compressedOrder));
        //System.err.println("Required bits: "+Arrays.toString(requiredBits));
        long compressedExpression = 
            ((compressedValueOrder & ((1L << requiredBits[0]) - 1)) |
            ((long) compressedOperations << requiredBits[0]) |  //casting to long to prevent overflow in n=7 case
            ((long) compressedOrder << (requiredBits[0] + requiredBits[1])));
        //System.err.println("Compressed expression: "+toBinary(compressedExpression));
        return compressedExpression;
    }
    public static Expression decompressExpression (long compressedExpression, int numValues) {
        int[] requiredBits= REQUIRED_BITS(numValues);
        byte[] valueOrder = intToValueOrder((int) (compressedExpression & ((1L<<requiredBits[0])-1)), numValues);
        byte[] operations = intToOperations((int) (compressedExpression >>> requiredBits[0] & ((1L<<requiredBits[1])-1)), numValues-1);
        boolean[] order = intToOrder((int) (compressedExpression >>> (requiredBits[0]+requiredBits[1]) & ((1L<<requiredBits[2])-1)), 2*numValues-4);
        //System.err.println("Compressed expression: "+toBinary(compressedExpression));

        //System.err.println("Decompressed value order: "+Arrays.toString(valueOrder));
        //System.err.println("Decompressed operations: "+Arrays.toString(operations));
        //System.err.println("Decompressed order: "+Arrays.toString(order));
        return new Expression(valueOrder, operations, order);

    }

    public static int orderToInt(boolean[] order) {
        int value = 0;
        for (int i = 2; i < order.length-1; i++) { // the first two values and last value of order must be true true false in order to make a valid RPN expression.
            value |= (order[i] ? 1 : 0) << (i-2);
        }
        return value;
    }
    public static boolean[] intToOrder(int value, int length) { //160
        boolean[] order = new boolean[length+3];
        order[0]= true; // the first two values and last value of order must be true true false in order to make a valid RPN expression.
        order[1]= true;
        order[length+2]= false;
        for (int i = 0; i < length; i++) {
            order[i+2] = ((value >> i) & 1) == 1;
        }
        return order;
    }
    public static int operationsToInt(byte[] operations) {

        //operations can be one of 4 values, so we can use 2 bits to store each operation
        // we can then just convert the byte array to an integer
        int value = 0;
        for (int i = 0; i < operations.length; i++) {
            value |= (operations[i] << (i*2));
        }
        return value;
    }

    public static byte[] intToOperations(int value, int length) {
        if (length>10) {
            throw new IllegalArgumentException("Length cannot be greater than 10");
        }
        byte[] operations = new byte[length];
        for (int i = 0; i < length; i++) {
            operations[i] = (byte) (value & 0b11);
            value >>= 2;
        }
        return operations;
    }
    public static int valueOrderToInt(byte[] valueOrder) {

        // https://stackoverflow.com/questions/1506078/fast-permutation-number-permutation-mapping-algorithms
        if (valueOrder.length>10) {
            throw new IllegalArgumentException("Value order length cannot be greater than 10");
        }
        int value = 0;
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

    public static byte[] intToValueOrder(int value, int length) {
        // https://stackoverflow.com/questions/1506078/fast-permutation-number-permutation-mapping-algorithms

        int[] permutation = new int[length];
        boolean[] seen = new boolean[length];
        int base = 2;

        

        for (int i = 0; i < length-1; i++) { //calculate the first n-1 values, since n=th is always 0
            
            permutation[length-i-2] = (value % base); 
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


    private static int factorial(int n) {
        int value = 1;
        for (int i = 2; i <= n; i++) {
            value *= i;
        }
        return value;
    }
}
