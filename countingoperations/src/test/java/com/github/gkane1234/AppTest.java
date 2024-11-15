package com.github.gkane1234;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class AppTest {
    public AppTest() {
    }

    @Test
    public void testExpressionCompression() {

        int numValues = 6;
        Solver s = new Solver(numValues, false, true, null);
        Random r = new Random();

       


        ExpressionSet e = new ExpressionSet(numValues,7,4);
        for (int i = 0; i < 10; i++) {
            int rIndex = (int)(r.nextDouble()*s.solverSet.getNumExpressions());
            Expression randomExpression = s.solverSet.get(rIndex);
            System.err.println(randomExpression);
            long compressedExpression = ExpressionCompression.compressExpression(randomExpression);
            System.err.println(compressedExpression);
            Expression decompressedExpression  = ExpressionCompression.decompressExpression(compressedExpression, numValues);
            e.forceAdd(decompressedExpression);
            Assert.assertTrue(randomExpression.equals(decompressedExpression));
        
        long[] compressedExpressionSet = ExpressionCompression.compressExpressionSet(e);
        ExpressionSet decompressedExpressionSet = ExpressionCompression.decompressExpressionSet(compressedExpressionSet, e.getNumExpressions(),numValues);
        Assert.assertTrue(e.equals(decompressedExpressionSet));
        
        
        }
    }


}
