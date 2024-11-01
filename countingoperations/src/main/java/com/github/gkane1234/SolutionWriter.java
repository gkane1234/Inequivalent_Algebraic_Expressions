package com.github.gkane1234;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class SolutionWriter {
    String filePath;
    List<SolutionSet> allSolutions;
    boolean fullOutput;
    String metadata;
    String fileName;
    public SolutionWriter(String filePath,List<SolutionSet> allSolutions,boolean fullOutput){
        this.filePath=filePath;
        this.allSolutions=allSolutions;
        this.fullOutput=fullOutput;
        createMetadataAndName();
    }

    public void createFile() {

        int solvable = 0;
        int i = 0;

        this.filePath+="/"+this.fileName;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(this.filePath))) {
            writer.println(this.metadata);
            for (SolutionSet solutionSet : this.allSolutions) {
                if (this.fullOutput) {
                    writer.println(SolutionWriter.createVerboseLine(solutionSet));
                }
                else {
                    writer.println(SolutionWriter.createSmallLine(solutionSet, i++));
                }
                if (solutionSet.getNumSolutions() > 0) {
                    solvable++;
                }
            }
            writer.println("Solvable count: " + solvable); // Write solvable count to the file
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private static String createVerboseLine(SolutionSet solutionSet) { 
        return solutionSet.toString();

    }
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
    private static String createSmallLine(SolutionSet solutionSet,int i) {
        return String.valueOf(i)+","+String.valueOf(solutionSet.getNumSolutions());
    }




}