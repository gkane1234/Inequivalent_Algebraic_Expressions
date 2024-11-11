package com.github.gkane1234;

public class Applet {
    private Solver solver;
    private javax.swing.JTextField goalField;
    private javax.swing.JTextField[] valueFields;
    private javax.swing.JTextArea solutionsArea;
    private javax.swing.JComboBox<Integer> numValuesCombo;
    private javax.swing.JPanel valuesPanel;
    private SolutionList currentSolutions;
    
    public Applet() {
        System.out.println("Max Memory: " + Runtime.getRuntime().maxMemory());
        System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory());

        javax.swing.JFrame frame = new javax.swing.JFrame("Expression Solver");
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new java.awt.BorderLayout());
        
        javax.swing.JPanel mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new java.awt.BorderLayout());
        
        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        topPanel.setLayout(new java.awt.GridLayout(3, 2));
        
        topPanel.add(new javax.swing.JLabel("Number of Values:"));
        Integer[] values = {2, 3, 4, 5, 6, 7};
        numValuesCombo = new javax.swing.JComboBox<>(values);
        numValuesCombo.setSelectedItem(7);
        numValuesCombo.addActionListener(e -> updateValueFields());
        topPanel.add(numValuesCombo);
        
        javax.swing.JButton loadButton = new javax.swing.JButton("Load/Create Solver");
        loadButton.addActionListener(e -> loadSolver());
        topPanel.add(loadButton);
        topPanel.add(new javax.swing.JLabel());
        
        topPanel.add(new javax.swing.JLabel("Goal:"));
        goalField = new javax.swing.JTextField();
        topPanel.add(goalField);
        
        mainPanel.add(topPanel, java.awt.BorderLayout.NORTH);
        

        valuesPanel = new javax.swing.JPanel();
        valuesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Values"));
        updateValueFields();
        mainPanel.add(valuesPanel, java.awt.BorderLayout.CENTER);
        

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        javax.swing.JButton findButton = new javax.swing.JButton("Find Solutions");
        findButton.addActionListener(e -> findSolutions());
        buttonPanel.add(findButton);
        
        javax.swing.JButton showButton = new javax.swing.JButton("Show Solutions");
        showButton.addActionListener(e -> showSolutions());
        buttonPanel.add(showButton);
        
        mainPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
        
        frame.add(mainPanel, java.awt.BorderLayout.NORTH);
        
        solutionsArea = new javax.swing.JTextArea();
        solutionsArea.setEditable(false);
        frame.add(new javax.swing.JScrollPane(solutionsArea), java.awt.BorderLayout.CENTER);
        
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void updateValueFields() {
        int numValues = (Integer)numValuesCombo.getSelectedItem();
        valueFields = new javax.swing.JTextField[numValues];
        valuesPanel.removeAll();
        valuesPanel.setLayout(new java.awt.FlowLayout());
        
        for (int i = 0; i < numValues; i++) {
            valueFields[i] = new javax.swing.JTextField(5);
            valuesPanel.add(valueFields[i]);
        }
        
        valuesPanel.revalidate();
        valuesPanel.repaint();
    }
    
    private void loadSolver() {
        int numValues = (Integer)numValuesCombo.getSelectedItem();
        solutionsArea.setText("Loading solver with " + numValues + " values...\n");
        
        new Thread(() -> {
            try {
                solver = new Solver(numValues, true, true);
                javax.swing.SwingUtilities.invokeLater(() -> 
                    solutionsArea.append("\nSolver loaded successfully!")
                );
            } catch (Exception ex) {
                javax.swing.SwingUtilities.invokeLater(() ->
                    solutionsArea.append("\nError loading solver: " + ex.getMessage())
                );
            }
        }).start();
    }
    
    private void findSolutions() {
        if (solver == null) {
            solutionsArea.setText("Please load the solver first!");
            return;
        }
        
        try {
            double goal = Double.parseDouble(goalField.getText());
            double[] values = new double[valueFields.length];
            
            for (int i = 0; i < valueFields.length; i++) {
                values[i] = Double.parseDouble(valueFields[i].getText().trim());
            }
            
            currentSolutions = solver.findAllSolutions(values, goal);
            solutionsArea.setText("Found " + currentSolutions.getNumSolutions() + " solutions!\nClick 'Show Solutions' to display them.");
            
        } catch (NumberFormatException ex) {
            solutionsArea.setText("Invalid input. Please enter valid numbers in all fields.");
        }
    }
    
    private void showSolutions() {
        if (currentSolutions == null) {
            solutionsArea.setText("Please find solutions first!");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(currentSolutions.getNumSolutions()).append(" solutions:\n\n");
        
        for (EvaluatedExpression solution : currentSolutions.getEvaluatedExpressionList()) {
            sb.append(solution.display()).append(" = ").append(currentSolutions.getGoal()).append("\n");
        }
        
        solutionsArea.setText(sb.toString());
    }
    
    public static void main(String[] args) {
        Applet applet = new Applet();
    }
}
