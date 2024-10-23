package counting_operations.Java_Version;

public class CountingMain {
    public static void main(String[] args) {
        // Start time
        long startTime = System.nanoTime();  // Use System.nanoTime() for higher precision

        // The lines you want to time
        int numValues = 7;
        //ExpressionDynamic expressionDynamic = new ExpressionDynamic(numValues, 6, 8, null);
        //ExpressionList expressionList = expressionDynamic.getExpressionList();
        Solver s = new Solver(numValues);
        //System.out.println(s.findAllSolutions(new double[]{1,4,3,9,7}, 8));

        // End time
        long endTime = System.nanoTime();

        // Calculate the duration in milliseconds (or nanoseconds, depending on your preference)
        long durationInNano = endTime - startTime;
        double durationInMillis = durationInNano / 1_000_000.0;  // Convert nanoseconds to milliseconds

        // Output the duration
        System.out.println("Execution time: " + durationInNano + " nanoseconds");
        System.out.println("Execution time: " + durationInMillis + " milliseconds");
        //System.err.println(expressionList);
        //System.err.println(expressionList.getExpressions().size());
        //System.err.println(Counter.run(numValues));
        
    }
    public static void print(Object[] objs) {
        for (Object obj : objs) {
            System.err.print(obj);
            System.err.print(", ");
        }
        System.err.println("");
    }
    public static void print(byte[] objs) {
        for (byte obj : objs) {
            System.err.print(obj);
            System.err.print(", ");
        }
        System.err.println("");
    }
    public static void print(boolean[] objs) {
        for (boolean obj : objs) {
            System.err.print(obj);
            System.err.print(", ");
        }
        System.err.println("");
    }


}