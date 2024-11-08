package com.github.gkane1234;

import java.util.Scanner;
import java.io.FileNotFoundException;
public class CountingMain {
    public static void main(String[] args) {

        //TODO: find ways to reduce memory strain:
            //Make each truncator work separately
            //Reduce the size of the range of numbers we're storing 
        // Start time
        long startTime = System.nanoTime();  // Use System.nanoTime() for higher precision

        // The lines you want to time
        
        //84.0, 18.0, 78.0, 91.0, 61.0, 87.0, to make 2027
        //(((((18/78)+84)-61)*91)-87)

        // 3.0, 4.0, 9.0, 14.0, 7.0, 16.0, to make 2027
        //((((4/7)+(16*9))*14)+3) 1=
        //ExpressionDynamic expressionDynamic = new ExpressionDynamic(numValues, 6, 8, null);
        //ExpressionList expressionList = expressionDynamic.getExpressionList();
        //{791,27,18632,1,19.315,-793.2,-8537.214}
        //System.out.println(CountingPossibilities.numberOfDistinctSetsOfNumbersWhereOrderDoesNotMatter(10,100));
        int numValues = 7;

        Scanner scanner = new Scanner(System.in);
        Solver s = new Solver(numValues,true,true);

        /* 24
        int[] range = {1,13};
        List<SolutionSet> questions = s.findSolvableValues(10,137,range,new int[]{1,10000});
        for (SolutionSet question : questions) {
            System.out.println(question);
        }
        */

        /*
        Solver s = new Solver(numValues); 
        List<SolutionSet> allSolutions = s.findAllPossibleSolvableValuesInRange(range, 24, true);

        SolutionWriter solutionWriter = new SolutionWriter("counting_operations/outputs", allSolutions, false);
        solutionWriter.createFile();
        */

        
        /*
         * For example the inequivalent expressions using the 2 variables a and b would be  f(a)⊕f(b) or    {a+b,a-b,b-a,ab,a/b,b/a} – these expressions can be evaluated for imputed values, such as at (5,12), which would evaluate to 17, -7, 7, 60, 5/12, and 12/5 respectively. So for 4 numbers, a,b,c, and d the space to check should be:

         * f(a)⊕f(b,c,d), f(b)⊕f(a,c,d), f(c)⊕f(a,b,d), f(d)⊕f(a,b,c)
         * f(a,b)⊕f(c,d), f(a,c)⊕f(b,d), f(a,d)⊕f(b,c)
         * 
         * 
         */
        

 
        
        while (true) {
            System.out.print("Enter a new goal value (or type 'exit' to quit): ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                double goal = Double.parseDouble(input);
                
                System.out.print("Enter " + numValues + " values separated by spaces: ");
                String[] valueStrings = scanner.nextLine().trim().split("\\s+");
                
                if (valueStrings.length != numValues) {
                    System.out.println("Please enter exactly " + numValues + " values.");
                    continue;
                }

                int[] values = new int[numValues];
                for (int i = 0; i < numValues; i++) {
                    values[i] = Integer.parseInt(valueStrings[i]);
                }

                SolutionSet solutions = s.findAllSolutions(values, goal);
                
                if (solutions.getNumSolutions() == 0) {
                    System.out.println("No solutions found for these values and goal.");
                } else {
                    System.out.println("Found " + solutions.getNumSolutions() + " solutions:");
                    for (Solution solution : solutions.getSolutions()) {
                        System.out.println(solution.display());
                    }
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter valid numbers.");
            }
        }

        scanner.close();
    





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
    public static void print(double[] objs) {
        for (double obj : objs) {
            System.err.print(obj);
            System.err.print(", ");
        }
        System.err.println("");
    }
    public static void print(int[] objs) {
        for (int obj : objs) {
            System.err.print(obj);
            System.err.print(", ");
        }
        System.err.println("");
    }


}