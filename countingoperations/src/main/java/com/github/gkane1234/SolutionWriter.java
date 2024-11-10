package com.github.gkane1234;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
    This class is used to write the solutions to a file.
*/
public class SolutionWriter {
    String filePath;
    List<SolutionList> allSolutions;
    boolean fullOutput;
    String metadata;
    String fileName;
    /**
        Constructor for the SolutionWriter class.
        @param filePath: a <code>String</code> representing the path to the file to write to.
        @param allSolutions: a <code>List<SolutionSet></code> representing the solutions to write to the file.
        @param fullOutput: a <code>boolean</code> representing whether to write the full output or a single number representing the expression, and a single number representing the number of solutions.
    */
    public SolutionWriter(String filePath,List<SolutionList> allSolutions,boolean fullOutput){
        this.filePath=filePath;
        this.allSolutions=allSolutions;
        this.fullOutput=fullOutput;
        createMetadataAndName();
    }

    /**
        Creates the file and writes the solutions to it.
    */
    public void createFile() {

        int solvable = 0;
        int i = 0;

        this.filePath+="/"+this.fileName;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(this.filePath))) {
            writer.println(this.metadata);
            for (SolutionList solutionList : this.allSolutions) {
                if (this.fullOutput) {
                    writer.println(SolutionWriter.createVerboseLine(solutionList));
                }
                else {
                    writer.println(SolutionWriter.createSmallLine(solutionList, i++));
                }
                if (solutionList.getNumSolutions() > 0) {
                    solvable++;
                }
            }
            writer.println("Solvable count: " + solvable); // Write solvable count to the file
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    /**
        Creates a verbose line for the file.
        @param solutionSet: a <code>SolutionSet</code> representing the solution set to write to the file.
        @return a <code>String</code> representing the solution set.
    */
    private static String createVerboseLine(SolutionList solutionList) { 
        return solutionList.toString();

    }
    /**
        Creates the metadata and name for the file.
    */
    private void createMetadataAndName() {
        String numValues = String.valueOf(allSolutions.get(0).getValues().length);
        String minValue = String.valueOf(allSolutions.get(0).getValues()[0]);
        String maxValue = String.valueOf(allSolutions.get(allSolutions.size()-1).getValues()[0]);
        String goal = String.valueOf(allSolutions.get(0).getGoal());
        this.metadata = "{'num_values': "+String.valueOf(numValues)
                        + ", 'min_value': "+String.valueOf(minValue)
                        + ", 'max_value': "+String.valueOf(maxValue)
                        + ", 'goal': "+String.valueOf(goal)+"}";
        
        this.fileName = "ALL_POSSIBLE_"+numValues+"_Values_"+minValue+"_TO_"+maxValue+"_GOAL_"+goal+".csv";
        

    }
    /**
        Creates a small line for the file.
        @param solutionSet: a <code>SolutionSet</code> representing the solution set to write to the file.
        @param i: an <code>int</code> representing the index of the solution set.
        @return a <code>String</code> representing the solution set.
    */
    private static String createSmallLine(SolutionList solutionList,int i) {
        return String.valueOf(i)+","+String.valueOf(solutionList.getNumSolutions());
    }




}