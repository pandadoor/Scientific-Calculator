package calculator.ui;

import calculator.engine.*;
import calculator.model.HistoryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CalculatorFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // UI COMPONENTS 
    private JTextField displayField;
    private JLabel angleModeLabel;
    private JList<String> historyList;
    private DefaultListModel<String> historyModel;
    private JButton clearHistoryButton;
    
    //  CALCULATOR ENGINE 
    private final MathContext context;
    private final ShuntingYardParser parser;
    private final RPNEvaluator evaluator;
    
    // HISTORY BUFFER 
    private final List<HistoryEntry> history;
    private static final int MAX_HISTORY = 100;
    
    // NUMBER FORMATTING
    private final DecimalFormat formatter;
    
    // CURRENT STATE
    private StringBuilder currentExpression;
    
    
    // COLOR SCHEME
    private static final Color OPERATOR_BG = new Color(34, 34, 34);
    private static final Color EQUALS_BG = new Color(34, 34, 34);
    private static final Color EQUALS_FG = Color.WHITE;
    
    /**
     * Constructs the calculator frame with all components.
     * 
     * INITIALIZATION ORDER:
     * 1. Engine components
     * 2. History buffer
     * 3. UI setup
     */
    public CalculatorFrame() {
        // Initialize engine components
        context = new MathContext();
        parser = new ShuntingYardParser(context);
        evaluator = new RPNEvaluator(context);
        
        // Initialize history
        history = new ArrayList<>();
        historyModel = new DefaultListModel<>();
        
        // Initialize formatter for display (with commas)
        formatter = new DecimalFormat("#,##0.##########");
        
        // Initialize expression builder
        currentExpression = new StringBuilder();
        
        // Setup UI components
        setupFrame();
        setupDisplay();
        setupButtons();
        setupHistory();
        
        pack();
        setLocationRelativeTo(null); // Center on screen
    }
    
    /**
     * Sets up the main frame properties.
     */
    private void setupFrame() {
        setTitle("Scientific Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(102, 102, 102));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel);
    }
    
    /**
     * Sets up the display panel (expression field + angle mode indicator).
     * Clean, borderless design with subtle shadows.
     */
    private void setupDisplay() {
        JPanel displayPanel = new JPanel(new BorderLayout(5, 5));
        displayPanel.setBackground(new Color(51, 51, 51));
        
        // Expression display (monospaced font for clarity)
        displayField = new JTextField();
        displayField.setForeground(new Color(255, 255, 255));
        displayField.setBackground(new Color(51, 51, 51));
        displayField.setFont(new Font("Monospaced", Font.BOLD, 24));
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setEditable(false);
        displayField.setFocusable(false);
        displayField.setPreferredSize(new Dimension(500, 60));
        displayField.setText("0");
        displayField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0), 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        // Angle mode indicator - clean, minimal design
        angleModeLabel = new JLabel(context.getAngleMode());
        angleModeLabel.setBackground(new Color(51, 51, 51));
        angleModeLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        angleModeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        angleModeLabel.setForeground(new Color(153, 204, 51));
        angleModeLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0), 1),
            new EmptyBorder(5, 15, 5, 15)
        ));
        
        displayPanel.add(displayField, BorderLayout.CENTER);
        displayPanel.add(angleModeLabel, BorderLayout.EAST);
        
        getContentPane().add(displayPanel, BorderLayout.NORTH);
    }
    
    /**
     * Sets up the button grid using GridBagLayout.
     * Creates a 6x7 grid of calculator buttons.
     * Applies accent colors to operators and equals button.
     */
    private void setupButtons() {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setForeground(new Color(255, 255, 255));
        buttonPanel.setBackground(new Color(102, 102, 102));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        // Button layout (6 rows x 7 columns)
        String[][] buttonLabels = {
            {"sin", "cos", "tan", "asin", "acos", "atan", "D/R"},
            {"ln", "log", "exp", "√", "x²", "xⁿ", "|x|"},
            {"π", "e", "(", ")", "C", "⌫", "÷"},
            {"7", "8", "9", "%", "1/x", "±", "×"},
            {"4", "5", "6", "+", "-", "(", ")"},
            {"1", "2", "3", ".", "0", "=", "="}
        };
        
        for (int row = 0; row < buttonLabels.length; row++) {
            for (int col = 0; col < buttonLabels[row].length; col++) {
                String label = buttonLabels[row][col];
                
                // Skip duplicate equals button space
                if (label.equals("=") && col > 5) {
                    continue;
                }
                
                JButton button = createButton(label);
                
                gbc.gridx = col;
                gbc.gridy = row;
                
                // Make equals button span 2 columns
                if (label.equals("=")) {
                    gbc.gridwidth = 2;
                }
                
                buttonPanel.add(button, gbc);
            }
        }
        
        getContentPane().add(buttonPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates a styled button with action handler.
     * Applies modern, minimalist styling with subtle rounded corners.
     * Operators and equals button receive accent colors.
     */
    private JButton createButton(String label) {
    	JButton button = new JButton(label);
        button.setForeground(Color.WHITE); //  font color
        button.setFont(new Font("Dialog", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(70, 50));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Apply accent colors to specific buttons
        if (label.equals("=")) {
            // Equals button: prominent accent
            button.setFont(new Font("Dialog", Font.BOLD, 18));
            button.setBackground(EQUALS_BG);
            button.setForeground(EQUALS_FG);
        } else if (isOperatorButton(label)) {
            // Operators: subtle accent background
            button.setBackground(OPERATOR_BG);
        }
        
        // Add hover effect for better UX
        button.addMouseListener(new MouseAdapter() {
            Color originalBg = button.getBackground();
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (label.equals("=")) {
                    button.setBackground(EQUALS_BG.brighter());
                } else if (isOperatorButton(label)) {
                    button.setBackground(OPERATOR_BG.brighter());
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBg);
            }
        });
        
        // Add action listener
        button.addActionListener(e -> handleButtonClick(label));
        
        return button;
    }
    
    /**
     * Determines if a button label represents an operator.
     * Used for applying accent colors.
     */
    private boolean isOperatorButton(String label) {
        return label.matches("[+\\-×÷%^]") || 
               label.equals("xⁿ") || 
               label.equals("x²") ||
               label.equals("1/x") ||
               label.equals("±") ||
               label.equals("|x|") ||
               label.equals("√");
    }
    
    /**
     * Handles button clicks and routes to appropriate action.
     */
    private void handleButtonClick(String label) {
        switch (label) {
            case "=":
                evaluateExpression();
                break;
            case "C":
                clearDisplay();
                break;
            case "⌫":
                backspace();
                break;
            case "D/R":
                toggleAngleMode();
                break;
            case "sin":
            case "cos":
            case "tan":
            case "asin":
            case "acos":
            case "atan":
            case "ln":
            case "log":
            case "exp":
            case "√":
                appendFunction(label);
                break;
            case "x²":
                appendOperator("^2");
                break;
            case "xⁿ":
                appendOperator("^");
                break;
            case "|x|":
                appendOperator("|");
                break;
            case "1/x":
                insertReciprocal();
                break;
            case "±":
                toggleSign();
                break;
            case "×":
                appendOperator("*");
                break;
            case "÷":
                appendOperator("/");
                break;
            default:
                appendToExpression(label);
                break;
        }
    }
    
    /**
     * Appends text to the current expression.
     */
    private void appendToExpression(String text) {
        if (currentExpression.toString().equals("0")) {
            currentExpression = new StringBuilder();
        }
        currentExpression.append(text);
        updateDisplay();
    }
    
    /**
     * Appends a function with opening parenthesis.
     */
    private void appendFunction(String func) {
        if (func.equals("√")) {
            appendToExpression("sqrt(");
        } else {
            appendToExpression(func + "(");
        }
    }
    
    /**
     * Appends an operator.
     */
    private void appendOperator(String op) {
        appendToExpression(op);
    }
    
    /**
     * Inserts reciprocal function.
     */
    private void insertReciprocal() {
        String current = currentExpression.toString();
        if (!current.isEmpty() && !current.equals("0")) {
            currentExpression = new StringBuilder("1/(" + current + ")");
            updateDisplay();
        }
    }
    
    /**
     * Toggles sign of current number.
     */
    private void toggleSign() {
        String current = currentExpression.toString();
        if (current.startsWith("-")) {
            currentExpression = new StringBuilder(current.substring(1));
        } else if (!current.isEmpty() && !current.equals("0")) {
            currentExpression = new StringBuilder("-" + current);
        }
        updateDisplay();
    }
    
    /**
     * Updates the display field with current expression.
     */
    private void updateDisplay() {
        String expr = currentExpression.toString();
        if (expr.isEmpty()) {
            displayField.setText("0");
        } else {
            displayField.setText(expr);
        }
    }
    
    /**
     * Clears the display and resets expression.
     */
    private void clearDisplay() {
        currentExpression = new StringBuilder();
        displayField.setText("0");
    }
    
    /**
     * Removes last character from expression.
     */
    private void backspace() {
        if (currentExpression.length() > 0) {
            currentExpression.deleteCharAt(currentExpression.length() - 1);
            updateDisplay();
        }
    }
    
    /**
     * Toggles between DEG and RAD angle mode.
     */
    private void toggleAngleMode() {
        context.toggleAngleMode();
        angleModeLabel.setText(context.getAngleMode());
    }
    
    /**
     * Evaluates the current expression using the engine pipeline.
     * 
     * PIPELINE: Input → Tokenize → Parse → Evaluate → Display
     */
    private void evaluateExpression() {
        try {
            String expression = currentExpression.toString();
            
            /*
             * "^-?\\d+(\\.\\d+)?$" regex a single number only (positive/negative, integer/decimal)
             */
            
            if (expression.isEmpty() || expression.equals("0") || expression.trim().matches("^-?\\d+(\\.\\d+)?$")) { 
            	return; 
            }
            
            // PIPELINE STEP 1: Tokenize
            List<String> tokens = Tokenizer.tokenize(expression);
            
            // PIPELINE STEP 2: Parse (infix to postfix)
            List<String> postfix = parser.toPostfix(tokens);
            
            // PIPELINE STEP 3: Evaluate
            double result = evaluator.evaluate(postfix);
            
            // PIPELINE STEP 4: Format and display
            String formattedResult = formatResult(result);
            displayField.setText(formattedResult);
            
            // Add to history
            addToHistory(expression, formattedResult);
            
            // Set result as new expression for chaining
            currentExpression = new StringBuilder(String.valueOf(result));
            
        } catch (ArithmeticException e) {
            displayField.setText("Math Error");
            currentExpression = new StringBuilder();
        } catch (IllegalArgumentException e) {
            displayField.setText("Syntax Error");
            currentExpression = new StringBuilder();
        } catch (Exception e) {
            displayField.setText("Error");
            currentExpression = new StringBuilder();
        }
    }
    
    /**
     * Formats result with comma separators.
     * Handles special cases: Infinity, NaN.
     */
    private String formatResult(double result) {
        if (Double.isInfinite(result)) {
            return "Infinity";
        }
        if (Double.isNaN(result)) {
            return "NaN";
        }
        
        // Use formatter for comma separation
        return formatter.format(result);
    }
    
    /**
     * Sets up the history panel with Clear History button.
     * Button is disabled when history is empty.
     */
    private void setupHistory() {
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setForeground(new Color(255, 255, 255));
        historyPanel.setBackground(new Color(51, 51, 51));
        historyPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        historyPanel.setPreferredSize(new Dimension(220, 0));
        
        // History title label
        JLabel historyLabel = new JLabel("History");
        historyLabel.setForeground(new Color(255, 255, 255));
        historyLabel.setBackground(new Color(51, 51, 51));
        historyLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        historyLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        // History list
        historyList = new JList<>(historyModel);
        historyList.setForeground(new Color(255, 255, 255));
        historyList.setBackground(new Color(51, 51, 51));
        historyList.setFont(new Font("Monospaced", Font.BOLD, 11));
        historyList.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Double-click to reuse expression
        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = historyList.getSelectedIndex();
                    if (index >= 0 && index < history.size()) {
                        HistoryEntry entry = history.get(index);
                        currentExpression = new StringBuilder(entry.getExpression());
                        updateDisplay();
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));
        
        // Clear History button - disabled when empty
        clearHistoryButton = new JButton("Clear History");
        clearHistoryButton.setForeground(new Color(153, 204, 51));
        clearHistoryButton.setBackground(new Color(51, 51, 51));
        clearHistoryButton.setFont(new Font("Dialog", Font.BOLD, 12));
        clearHistoryButton.setEnabled(false); // Initially disabled
        clearHistoryButton.setFocusPainted(false);
        clearHistoryButton.addActionListener(e -> clearHistory());
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setForeground(new Color(255, 255, 255));
        topPanel.setBackground(new Color(51, 51, 51));
        topPanel.add(historyLabel, BorderLayout.NORTH);
        
        historyPanel.add(topPanel, BorderLayout.NORTH);
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        historyPanel.add(clearHistoryButton, BorderLayout.SOUTH);
        
        getContentPane().add(historyPanel, BorderLayout.EAST);
    }
    
    /**
     * Adds a calculation to the history buffer.
     * Maintains max size of 100 entries.
     * Enables Clear History button when history is not empty.
     */
    private void addToHistory(String expression, String result) {
        HistoryEntry entry = new HistoryEntry(expression, result);
        history.add(0, entry); // Add to beginning
        historyModel.add(0, entry.toString());
        
        // Maintain max size
        if (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
            historyModel.remove(historyModel.size() - 1);
        }
        
        // Enable Clear History button
        clearHistoryButton.setEnabled(true);
    }
    
    //Clears all history entries and disables the Clear History button.
    private void clearHistory() {
        if (!history.isEmpty()) {
            // Show confirmation dialog
            int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all history?",
                "Clear History",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                history.clear();
                historyModel.clear();
                clearHistoryButton.setEnabled(false);
            }
        }
    }
}