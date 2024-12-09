package com.github.gkane1234;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;

public class CountingOperationsApplet implements SolverUpdateListener {
    private static final String SAVE_FILE_PATH = "counting_operations/outputs/saved_solutions/";
    private Solver solver;
    private SolutionList currentSolutions;
    
    private List<SolutionList> savedSolutionLists;
    private SolutionWriter savedSolutionWriter;

    private javax.swing.JTextField goalField;
    private javax.swing.JTextField[] valueFields;
    private javax.swing.JPanel solutionsPanel;
    private javax.swing.JTextArea[] foundValueTextAreas;
    private javax.swing.JPanel foundValuesPanel;
    private javax.swing.JPanel findValuesPanel;
    private javax.swing.JTextArea debugArea;
    private javax.swing.JComboBox<Integer> numValuesCombo;
    private javax.swing.JPanel valuesPanel;
    private javax.swing.JComboBox<String> modeCombo;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JTextField minValueField;
    private javax.swing.JTextField maxValueField;
    private javax.swing.JTextField minSolutionsField;
    private javax.swing.JTextField maxSolutionsField;
    private javax.swing.JButton findButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton addToSavedButton;
    private javax.swing.JButton clearSavedButton;
    private javax.swing.JPanel solutionDisplayPanel;
    private javax.swing.JTextArea solutionTextArea;
    private javax.swing.JToggleButton toggleSolutionsButton;
    private SolutionFinderWorker currentWorker;
    
    public CountingOperationsApplet() {
        System.out.println("Max Memory: " + Runtime.getRuntime().maxMemory());
        System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory());
        createJFrame();

        savedSolutionLists = new ArrayList<>();
        savedSolutionWriter = null;
    }

    private void createJFrame() {
        javax.swing.JFrame frame = new javax.swing.JFrame("Expression Solver");
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new java.awt.BorderLayout());
        
        javax.swing.JPanel mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new java.awt.BorderLayout());
        
        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        topPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        
        javax.swing.JPanel modePanel = new javax.swing.JPanel();
        modePanel.add(new javax.swing.JLabel("Mode:"));
        String[] modes = {"Specific Values", "Find Values"};
        modeCombo = new javax.swing.JComboBox<>(modes);
        modeCombo.setPreferredSize(new java.awt.Dimension(150, 25));
        modeCombo.addActionListener(e -> updateMode());
        modePanel.add(modeCombo);
        topPanel.add(modePanel);
        
        javax.swing.JPanel valuesCountPanel = new javax.swing.JPanel();
        valuesCountPanel.add(new javax.swing.JLabel("Number of Values:"));
        Integer[] values = {2, 3, 4, 5, 6, 7};
        numValuesCombo = new javax.swing.JComboBox<>(values);
        numValuesCombo.setPreferredSize(new java.awt.Dimension(80, 25));
        numValuesCombo.setSelectedItem(7);
        numValuesCombo.addActionListener(e -> {
            updateValueFields((Integer)numValuesCombo.getSelectedItem(), valuesPanel, true);
            updateValueFields((Integer)numValuesCombo.getSelectedItem(), foundValuesPanel, false);
        });
        valuesCountPanel.add(numValuesCombo);
        topPanel.add(valuesCountPanel);
        
        javax.swing.JButton loadButton = new javax.swing.JButton("Load/Create Solver");
        loadButton.setPreferredSize(new java.awt.Dimension(150, 25));
        loadButton.addActionListener(e -> loadSolver());
        topPanel.add(loadButton);
        
        javax.swing.JPanel goalPanel = new javax.swing.JPanel();
        goalPanel.add(new javax.swing.JLabel("Goal:"));
        goalField = new javax.swing.JTextField();
        goalField.setPreferredSize(new java.awt.Dimension(100, 25));
        goalPanel.add(goalField);
        topPanel.add(goalPanel);
        
        mainPanel.add(topPanel, java.awt.BorderLayout.NORTH);
        
        inputPanel = new javax.swing.JPanel(new java.awt.CardLayout());
        
        // Specific values panel
        valuesPanel = new javax.swing.JPanel();
        valuesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Values"));
        updateValueFields((Integer)numValuesCombo.getSelectedItem(), valuesPanel, true);
        
        // Found values panel
        foundValuesPanel = new javax.swing.JPanel();
        foundValuesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Found Values"));
        updateValueFields((Integer)numValuesCombo.getSelectedItem(), foundValuesPanel, false);
        
        // Find values panel
        findValuesPanel = new javax.swing.JPanel();
        findValuesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Value Constraints"));
        findValuesPanel.setLayout(new java.awt.GridLayout(4, 2, 5, 5));
        
        findValuesPanel.add(new javax.swing.JLabel("Min Value:"));
        minValueField = new javax.swing.JTextField("1");
        findValuesPanel.add(minValueField);
        
        findValuesPanel.add(new javax.swing.JLabel("Max Value:"));
        maxValueField = new javax.swing.JTextField("100");
        findValuesPanel.add(maxValueField);
        
        findValuesPanel.add(new javax.swing.JLabel("Min Solutions:"));
        minSolutionsField = new javax.swing.JTextField("1");
        findValuesPanel.add(minSolutionsField);
        
        findValuesPanel.add(new javax.swing.JLabel("Max Solutions:"));
        maxSolutionsField = new javax.swing.JTextField("10");
        findValuesPanel.add(maxSolutionsField);
        
        inputPanel.add(valuesPanel, "Specific Values");
        inputPanel.add(findValuesPanel, "Find Values");
        mainPanel.add(inputPanel, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));
        
        findButton = new javax.swing.JButton("Find Solutions");
        findButton.setPreferredSize(new java.awt.Dimension(120, 25));
        findButton.addActionListener(e -> {
            if (currentWorker != null && !currentWorker.isDone()) {
                broadcast("Cancelling solution search...");
                currentWorker.cancel(true);
                findButton.setText(modeCombo.getSelectedItem().equals("Find Values") ? "Find Values" : "Find Solutions");
            } else {
                broadcast("Finding solutions...");
                findButton.setText("Cancel");
                currentWorker = new SolutionFinderWorker();
                currentWorker.execute();
            }
        });
        buttonPanel.add(findButton);
        
        toggleSolutionsButton = new javax.swing.JToggleButton("Show Solutions");

        toggleSolutionsButton.addActionListener(e -> toggleSolutions());
        buttonPanel.add(toggleSolutionsButton);
        
        addToSavedButton = new javax.swing.JButton("Add to Saved");

        addToSavedButton.addActionListener(e -> addToSaved());
        buttonPanel.add(addToSavedButton);

        clearSavedButton = new javax.swing.JButton("Clear Saved");

        clearSavedButton.addActionListener(e -> clearSaved());
        buttonPanel.add(clearSavedButton);

        saveButton = new javax.swing.JButton("Save to File");

        saveButton.addActionListener(e -> saveSolutions());
        buttonPanel.add(saveButton);

        mainPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
        
        frame.add(mainPanel, java.awt.BorderLayout.NORTH);
        
        // Create split pane for solutions and debug areas
        javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT);
        
        // Solutions panel
        solutionsPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        solutionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Solutions"));
        
        // Found values display
        solutionsPanel.add(foundValuesPanel, java.awt.BorderLayout.NORTH);
        
        // Solution display panel (initially hidden)
        solutionDisplayPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        solutionTextArea = new javax.swing.JTextArea();
        solutionTextArea.setEditable(false);
        solutionDisplayPanel.add(new javax.swing.JScrollPane(solutionTextArea), java.awt.BorderLayout.CENTER);
        solutionDisplayPanel.setVisible(false);
        solutionsPanel.add(solutionDisplayPanel, java.awt.BorderLayout.CENTER);

        splitPane.setTopComponent(solutionsPanel);
        
        debugArea = new javax.swing.JTextArea();
        debugArea.setEditable(false);
        splitPane.setBottomComponent(new javax.swing.JScrollPane(debugArea));
        
        splitPane.setResizeWeight(0.7); // Give 70% space to solutions area by default
        
        frame.add(splitPane, java.awt.BorderLayout.CENTER);
        
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private void updateMode() {
        java.awt.CardLayout cl = (java.awt.CardLayout)(inputPanel.getLayout());
        cl.show(inputPanel, (String)modeCombo.getSelectedItem());
        
        // Update button text based on mode
        if (modeCombo.getSelectedItem().equals("Find Values")) {
            findButton.setText("Find Values");
        } else {
            findButton.setText("Find Solutions");
        }
    }
    
    private void updateValueFields(int numValues, javax.swing.JPanel valuesPanel, boolean editable) {
        if (valuesPanel == this.valuesPanel) {
            valueFields = new javax.swing.JTextField[numValues];
        } else if (valuesPanel == foundValuesPanel) {
            foundValueTextAreas = new javax.swing.JTextArea[numValues];
        }
        
        valuesPanel.removeAll();
        valuesPanel.setLayout(new java.awt.FlowLayout());
        
        for (int i = 0; i < numValues; i++) {
            if (valuesPanel == this.valuesPanel) {
                valueFields[i] = new javax.swing.JTextField(5);
                valueFields[i].setEditable(editable);
                valuesPanel.add(valueFields[i]);
            } else if (valuesPanel == foundValuesPanel) {
                foundValueTextAreas[i] = new javax.swing.JTextArea(1, 5);
                foundValueTextAreas[i].setEditable(editable);
                valuesPanel.add(foundValueTextAreas[i]);
            }
        }
        
        valuesPanel.revalidate();
        valuesPanel.repaint();
    }
    
    private void loadSolver() {
        int numValues = (Integer)numValuesCombo.getSelectedItem();
        broadcast("Loading solver with " + numValues + " values...");
        
        new Thread(() -> {
            try {
                solver = new Solver(numValues, true, true, this,true);

                javax.swing.SwingUtilities.invokeLater(() -> 
                    broadcast("\nSolver loaded successfully!")
                );
            } catch (Exception ex) {
                javax.swing.SwingUtilities.invokeLater(() ->
                    broadcast("\nError loading solver: " + ex.getMessage())
                );
            }
        }).start();
    }

    private class SolutionFinderWorker extends SwingWorker<SolutionList, String> {
        @Override
        protected SolutionList doInBackground() throws Exception {
            if (solver == null || solver.getNumValues() != (Integer)numValuesCombo.getSelectedItem()) {
                publish("Solver for " + numValuesCombo.getSelectedItem() + " values not loaded! Loading solver...");
                solver = new Solver((Integer)numValuesCombo.getSelectedItem(), true, true, CountingOperationsApplet.this, true);
            }

            double goal = Double.parseDouble(goalField.getText());
            solutionDisplayPanel.setVisible(false);
            toggleSolutionsButton.setSelected(false);

            if (modeCombo.getSelectedItem().equals("Specific Values")) {
                double[] values = new double[valueFields.length];
                for (int i = 0; i < valueFields.length; i++) {
                    values[i] = Double.parseDouble(valueFields[i].getText().trim());
                }
                return solver.findAllSolutions(values, goal, 200);
            } else {
                int minValue = Integer.parseInt(minValueField.getText().trim());
                int maxValue = Integer.parseInt(maxValueField.getText().trim());
                int minSolutions = Integer.parseInt(minSolutionsField.getText().trim());
                int maxSolutions = Integer.parseInt(maxSolutionsField.getText().trim());
                
                int[] valueRange = {minValue, maxValue};
                int[] solutionRange = {minSolutions, maxSolutions};
                return solver.findSolvableValues(goal, valueRange, solutionRange);
            }
        }

        @Override
        protected void process(List<String> chunks) {
            for (String message : chunks) {
                broadcast(message);
            }
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    currentSolutions = get();
                    if (modeCombo.getSelectedItem().equals("Specific Values")) {
                        for (int i = 0; i < valueFields.length; i++) {
                            foundValueTextAreas[i].setText(valueFields[i].getText().trim());
                        }
                        broadcast("Found " + currentSolutions.getNumSolutions() + " solutions!\nToggle 'Show Solutions' to display them.");
                    } else {
                        double[] foundValues = currentSolutions.getEvaluatedExpressionList().get(0).getValues();
                        for (int i = 0; i < foundValues.length; i++) {
                            foundValueTextAreas[i].setText(String.valueOf(foundValues[i]));
                        }
                        broadcast("Found values displayed above.\nToggle 'Show Solutions' to display the solutions.");
                    }
                } else {
                    broadcast("Solution search cancelled.");
                }
            } catch (Exception e) {
                broadcast("Error finding solutions: " + e.getMessage());
                if (modeCombo.getSelectedItem().equals("Find Values")) {
                    broadcast("Please enter valid numbers for the goal, and the values.");
                } else {
                    broadcast("Please enter valid numbers for the goal, value range, and solution range.");
                }
            } finally {
                findButton.setText(modeCombo.getSelectedItem().equals("Find Values") ? "Find Values" : "Find Solutions");
            }
        }
    }
    
    private void toggleSolutions() {
        if (currentSolutions == null) {
            broadcast("Please find solutions first!");
            toggleSolutionsButton.setSelected(false);
            return;
        }
        
        if (toggleSolutionsButton.isSelected()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Found ").append(currentSolutions.getNumSolutions()).append(" solutions:\n\n");
            
            for (EvaluatedExpression solution : currentSolutions.getEvaluatedExpressionList()) {
                sb.append(solution.display()).append(" = ").append(currentSolutions.getGoal()).append("\n");
            }
            
            solutionTextArea.setText(sb.toString());
            solutionDisplayPanel.setVisible(true);
            toggleSolutionsButton.setText("Hide Solutions");
        } else {
            solutionDisplayPanel.setVisible(false);
            toggleSolutionsButton.setText("Show Solutions");
        }
    }

    private void saveSolutions() {
        if (savedSolutionLists.isEmpty()) {
            broadcast("Please find solutions first!");
            return;
        }
        broadcast("Saving solutions to file...");
        savedSolutionWriter = new SolutionWriter(SAVE_FILE_PATH, savedSolutionLists, true, true);
        savedSolutionWriter.createFile();

        broadcast("Saved solutions to file!");
    }

    private void addToSaved() {
        if (currentSolutions == null) {
            broadcast("Please find solutions first!");
            return;
        }
        broadcast("Adding solutions to saved list...");
        savedSolutionLists.add(currentSolutions);
        broadcast("Added solutions to saved list!");
    }

    private void clearSaved() {
        broadcast("Clearing saved list...");
        savedSolutionLists.clear();
        broadcast("Saved list cleared!");
    }

    @Override
    public void onSolverUpdate(String update) {
        broadcast(update);
    }

    private void broadcast(String message) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            debugArea.append("\n" + message);
        });
    }
    
    public static void main(String[] args) {
        //TODO: make the gui better.
        //TODO: fix a problem where it finds that there are a valid number of solutions when there are more solutions than the max.
        // TODO: fix duplicate solutions appearing in the solutions list.
        javax.swing.SwingUtilities.invokeLater(() -> {
            new CountingOperationsApplet();
        });
    }
}
