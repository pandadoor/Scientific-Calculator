package calculator.ui;

import calculator.engine.*;
import calculator.model.HistoryEntry;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Main calculator UI frame.
 * Built with FlatLaf for modern, minimalist design.
 * Implements keyboard shortcuts and history buffer.
 * 
 * FILE TYPE: JFrame (Swing GUI Component)
 * PURPOSE: User interface for scientific calculator
 * 
 * FEATURES:
 * - FlatLaf Dark/Light theme toggle
 * - Monospaced display font
 * - Keyboard shortcuts (Enter, Backspace, Ctrl+L, Up arrow)
 * - History buffer (max 100 entries)
 * - Comma-formatted display
 * - DEG/RAD angle mode toggle
 */
public class CalculatorFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // ========== UI COMPONENTS ==========
    private JTextField displayField;
    private JLabel angleModeLabel;
    private JList<String> historyList;
    private DefaultListModel<String> historyModel;
    @SuppressWarnings("unused")
	private JButton themeToggleButton;
    
    // ========== CALCULATOR ENGINE ==========
    private final MathContext context;
    private final ShuntingYardParser parser;
    private final RPNEvaluator evaluator;
    
    // ========== HISTORY BUFFER ==========
    private final List<HistoryEntry> history;
    private static final int MAX_HISTORY = 100;
    
    // ========== NUMBER FORMATTING ==========
    private final DecimalFormat formatter;
    
    // ========== CURRENT STATE ==========
    private StringBuilder currentExpression;
    private int historyIndex = -1;
    private boolean isDarkTheme = true;
    
    /**
     * Constructs the calculator frame with all components.
     * 
     * INITIALIZATION ORDER:
     * 1. Engine components
     * 2. History buffer
     * 3. UI setup
     * 4. Event handlers
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
        setupKeyboardShortcuts();
        
        pack();
        setLocationRelativeTo(null); // Center on screen
    }
    
    /**
     * Sets up the main frame properties.
     */
    private void setupFrame() {
        setTitle("Scientific Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel);
    }
    
    /**
     * Sets up the display panel (expression field + angle mode indicator).
     */
    private void setupDisplay() {
        JPanel displayPanel = new JPanel(new BorderLayout(5, 5));
        
        // Expression display (monospaced font for clarity)
        displayField = new JTextField();
        displayField.setFont(new Font("Monospaced", Font.PLAIN, 24));
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setEditable(false);
        displayField.setFocusable(false);
        displayField.setPreferredSize(new Dimension(500, 60));
        displayField.setText("0");
        
        // Angle mode indicator
        angleModeLabel = new JLabel(context.getAngleMode());
        angleModeLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        angleModeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        angleModeLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        displayPanel.add(displayField, BorderLayout.CENTER);
        displayPanel.add(angleModeLabel, BorderLayout.EAST);
        
        getContentPane().add(displayPanel, BorderLayout.NORTH);
    }
    
    /**
     * Sets up the button grid using GridBagLayout.
     * Creates a 6x7 grid of calculator buttons.
     */
    private void setupButtons() {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        // Button layout (6 rows x 7 columns)
        String[][] buttonLabels = {
            {"sin", "cos", "tan", "asin", "acos", "atan", "DEG/RAD"},
            {"ln", "log", "exp", "√", "x²", "xⁿ", "|x|"},
            {"π", "e", "(", ")", "C", "⌫", "÷"},
            {"7", "8", "9", "%", "1/x", "±", "×"},
            {"4", "5", "6", "+", "-", "Theme", " "},
            {"1", "2", "3", ".", "0", "=", " "}
        };
        
        for (int row = 0; row < buttonLabels.length; row++) {
            for (int col = 0; col < buttonLabels[row].length; col++) {
                String label = buttonLabels[row][col];
                
                if (label.equals(" ")) {
                    // Empty space
                    continue;
                }
                
                JButton button = createButton(label);
                
                gbc.gridx = col;
                gbc.gridy = row;
                gbc.gridwidth = 1;
                
                // Make equals button span 2 columns if in last row
                if (label.equals("=") && row == buttonLabels.length - 1) {
                    gbc.gridwidth = 2;
                }
                
                buttonPanel.add(button, gbc);
            }
        }
        
        getContentPane().add(buttonPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates a styled button with action handler.
     */
    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Dialog", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(70, 50));
        button.setFocusPainted(false);
        
        // Special styling for equals button
        if (label.equals("=")) {
            button.setFont(new Font("Dialog", Font.BOLD, 18));
        }
        
        // Store reference to theme button
        if (label.equals("Theme")) {
            themeToggleButton = button;
        }
        
        // Add action listener
        button.addActionListener(e -> handleButtonClick(label));
        
        return button;
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
            case "DEG/RAD":
                toggleAngleMode();
                break;
            case "Theme":
                toggleTheme();
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
     * Applies comma formatting for better readability.
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
     * Toggles between Dark and Light theme.
     */
    private void toggleTheme() {
        try {
            if (isDarkTheme) {
                FlatLightLaf.setup();
            } else {
                FlatDarkLaf.setup();
            }
            SwingUtilities.updateComponentTreeUI(this);
            isDarkTheme = !isDarkTheme;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Evaluates the current expression using the engine pipeline.
     * 
     * PIPELINE: Input → Tokenize → Parse → Evaluate → Display
     */
    private void evaluateExpression() {
        try {
            String expression = currentExpression.toString();
            
            if (expression.isEmpty() || expression.equals("0")) {
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
     * Sets up the history panel.
     */
    private void setupHistory() {
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("History"));
        historyPanel.setPreferredSize(new Dimension(200, 0));
        
        historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
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
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        
        getContentPane().add(historyPanel, BorderLayout.EAST);
    }
    
    /**
     * Adds a calculation to the history buffer.
     * Maintains max size of 100 entries.
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
    }
    
    /**
     * Sets up keyboard shortcuts.
     * 
     * SHORTCUTS:
     * - Enter: Evaluate expression
     * - Backspace: Delete last character
     * - Ctrl+L: Clear display
     * - Up arrow: Previous history entry
     */
    private void setupKeyboardShortcuts() {
        // Get input map for the content pane
        JComponent contentPane = (JComponent) getContentPane();
        InputMap inputMap = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = contentPane.getActionMap();
        
        // Enter key: evaluate
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "evaluate");
        actionMap.put("evaluate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                evaluateExpression();
            }
        });
        
        // Backspace: delete
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "backspace");
        actionMap.put("backspace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backspace();
            }
        });
        
        // Ctrl+L: clear
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "clear");
        actionMap.put("clear", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearDisplay();
            }
        });
        
        // Up arrow: previous history
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "historyUp");
        actionMap.put("historyUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!history.isEmpty()) {
                    historyIndex = (historyIndex + 1) % history.size();
                    HistoryEntry entry = history.get(historyIndex);
                    currentExpression = new StringBuilder(entry.getExpression());
                    updateDisplay();
                }
            }
        });
    }
}