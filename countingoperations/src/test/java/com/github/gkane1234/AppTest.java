package com.github.gkane1234;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;

public class AppTest {
    public AppTest() {
    }

    @Test
    public void testExpressionCompression() {

        int numValues = 5;
        int numExpressions = 10;
        Solver s = new Solver(numValues, false, true, null);
        Random r = new Random();

       



        Expression[] expressions = new Expression[numExpressions];

        for (int i = 0; i < numExpressions; i++) {
            int rIndex = (int)(r.nextDouble()*s.solverSet.getNumExpressions());
            Expression randomExpression = s.solverSet.get(rIndex);
            System.err.println(randomExpression);
            System.err.println(i);
            long compressedExpression = ExpressionCompression.compressExpression(randomExpression);
            System.err.println(compressedExpression);
            Expression decompressedExpression  = ExpressionCompression.decompressExpression(compressedExpression, numValues);
            expressions[i]=randomExpression;
            Assert.assertTrue(randomExpression.equals(decompressedExpression));
        }

        ExpressionSet e = new ExpressionSet(expressions,expressions.length,numValues);
        System.err.println(Arrays.toString(expressions));
        System.err.println(e.getNumExpressions());
        System.err.println(numExpressions);
        long[] compressedExpressionSet = ExpressionCompression.compressExpressionSet(e);

        ExpressionSet decompressedExpressionSet = ExpressionCompression.decompressExpressionSet(compressedExpressionSet, e.getNumExpressions(),numValues);
        System.err.println(decompressedExpressionSet);
        Assert.assertTrue(e.equals(decompressedExpressionSet));
        
        
        
    }


}
