package com.github.gkane1234;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;



public class ExpressionCompression {
    public static int[] REQUIRED_BITS(int numValues) {
        int[] requiredBitsTotal = {0,4,10,16,22,28,35,42,49,56};
        int[] requiredBitsValueOrders = {0,1,3,5,7,9,12,15,18,21};
        int requiredBitsOperations = 2*(numValues)-2;
        int requiredBitsOrder = Math.max(2*(numValues)-4, 0);
        return new int[]{requiredBitsValueOrders[numValues],
                        requiredBitsOperations,
                        requiredBitsOrder,
                        requiredBitsTotal[numValues]};
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
            if (64-currentBit<requiredBits[3]) {
                currentBit = requiredBits[3] - (64-currentBit);
                currentLong++;
                compressedExpressions[currentLong] = compressedExpression >> (64-currentBit);
            }
            else{
                currentBit += requiredBits[3];
            }
        }
        return compressedExpressions;
        
    }
    public static ExpressionSet decompressExpressionSet (long[] compressedExpressions, int numCompressedExpressions, int numValues) {
        int[] requiredBits= REQUIRED_BITS(numValues);

        int currentLong = 0;
        int currentBit = 0;

        List<Expression> decompressedExpressions = new ArrayList<>();
        
        for (int i=0; i<compressedExpressions.length; i++) {
            long currentCompressedExpression = compressedExpressions[currentLong] >> currentBit;
            if (64-currentBit<requiredBits[3]) {
                currentBit = requiredBits[3] - (64-currentBit);
                currentLong++;
                currentCompressedExpression |= (compressedExpressions[currentLong] << currentBit);
            }
            else{
                currentBit += requiredBits[3];
                currentCompressedExpression &= ((1L<<requiredBits[3])-1);
            }
            decompressedExpressions.add(decompressExpression(currentCompressedExpression, numValues));
        }
        Expression[] decompressedExpressionsArray = new Expression[decompressedExpressions.size()];
        for (int i = 0; i < decompressedExpressions.size(); i++) {
            System.out.println(decompressedExpressions.get(i));

        }
        return new ExpressionSet(decompressedExpressions.toArray(decompressedExpressionsArray),numCompressedExpressions, numValues);

            



    }
    public static long compressExpression (Expression toCompress) {
        int numValues = toCompress.valueOrder.length;
        int[] requiredBits= REQUIRED_BITS(numValues);
        int compressedOrder = orderToInt(toCompress.order);
        int compressedOperations = operationsToInt(toCompress.operations);
        int compressedValueOrder = valueOrderToInt(toCompress.valueOrder);
        System.err.println(compressedValueOrder);
        System.err.println(compressedOperations);
        System.err.println(compressedOrder);
        long compressedExpression = ((compressedValueOrder & ((1L<<requiredBits[0])-1)) | (compressedOperations << requiredBits[0]) | (compressedOrder << (requiredBits[0]+requiredBits[1])));
        return compressedExpression;
    }
    public static Expression decompressExpression (long compressedExpression, int numValues) {
        int[] requiredBits= REQUIRED_BITS(numValues);
        System.err.println(compressedExpression & ((1L<<requiredBits[0])-1));
        System.err.println(compressedExpression >> requiredBits[0] & ((1L<<requiredBits[1])-1));
        System.err.println(compressedExpression >> (requiredBits[0]+requiredBits[1]) & ((1L<<requiredBits[2])-1));
        byte[] valueOrder = intToValueOrder((int) (compressedExpression & ((1L<<requiredBits[0])-1)), numValues);
        byte[] operations = intToOperations((int) (compressedExpression >> requiredBits[0] & ((1L<<requiredBits[1])-1)), numValues-1);
        boolean[] order = intToOrder((int) (compressedExpression >> (requiredBits[0]+requiredBits[1]) & ((1L<<requiredBits[2])-1)), 2*numValues-4);
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
        System.err.println(Arrays.toString(simplifiedValueOrder));
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
            System.err.println(Arrays.toString(permutation));
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
