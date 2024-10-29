package com.github.gkane1234;

import java.util.Scanner;

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
        //((((4/7)+(16*9))*14)+3) 1
        //ExpressionDynamic expressionDynamic = new ExpressionDynamic(numValues, 6, 8, null);
        //ExpressionList expressionList = expressionDynamic.getExpressionList();
        //{791,27,18632,1,19.315,-793.2,-8537.214}
        Scanner scanner = new Scanner(System.in);

        int numValues = 6;
        Solver s = new Solver(numValues); // Assuming Solver is defined elsewhere

        double[] values = new double[numValues];
        int[] range = {0, 10000}; // Your predefined range


        while (true) {
            System.out.println("Enter " + numValues + " values (type 'q' to quit):");
            boolean exitLoop = false;

            // Loop to get user inputs for the array
            for (int i = 0; i < numValues; i++) {
                if (scanner.hasNextDouble()) {
                    values[i] = scanner.nextDouble();
                } else if (scanner.hasNext("q")) {
                    exitLoop = true;
                    break;
                } else {
                    System.out.println("Invalid input, please enter a valid double or 'q' to quit.");
                    scanner.next(); // Clear the invalid input
                    i--; // Decrease index to retry for the same position
                }
            }

            if (exitLoop) {
                break; // Exit the loop if the user types 'q'
            }

            // Call the solver method after input
            System.out.println("First in range: " + s.findFirstInRange(values, range, true,true));
        }

        scanner.close();
        /* 
        while (true) {
            System.out.print("Enter a new goal value (or type 'exit' to quit): ");
            String input = scanner.nextLine();


            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                double goal = Double.parseDouble(input);
                List<List<Solution>> questions = s.findSolvableValues(1, goal, new int[] {1, 10}, new int[] {1, 300});

                for (List<Solution> question : questions) {
                    if (!question.isEmpty()) {
                        print(question.get(0).getValues());
                        System.out.print(question.get(0).display() + " Number of Solutions: ");
                        System.out.println(question.size());
                    } else {
                        System.out.println("No solutions found for goal: " + goal);
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number for the goal.");
            }
        }

        scanner.close(); // */ 
    





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