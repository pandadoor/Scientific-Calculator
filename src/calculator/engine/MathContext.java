package calculator.engine;

import calculator.model.Operator;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores global calculator state and configuration.
 * Manages angle mode (DEG/RAD) and operator definitions.
 * 
 * FILE TYPE: Regular Java Class (Configuration/Context holder)
 * PURPOSE: Central configuration for calculator engine
 */
public class MathContext {
    private boolean isDegreeMode = true; // Default to degrees
    private final Map<String, Operator> operators;
    
    /**
     * Initializes the math context with default settings.
     * Sets up operator precedence rules.
     */
    public MathContext() {
        operators = new HashMap<>();
        initializeOperators();
    }
    
    /**
     * Initializes operator precedence and associativity rules.
     * 
     * PRECEDENCE LEVELS:
     * 1 (lowest)  - Addition, Subtraction
     * 2           - Multiplication, Division, Modulo
     * 3           - Unary operators
     * 4 (highest) - Power (exponentiation)
     */
    private void initializeOperators() {
        // Precedence 1: Addition, Subtraction (left associative)
        operators.put("+", new Operator(1, true));
        operators.put("-", new Operator(1, true));
        
        // Precedence 2: Multiplication, Division, Modulo (left associative)
        operators.put("*", new Operator(2, true));
        operators.put("/", new Operator(2, true));
        operators.put("%", new Operator(2, true));
        
        // Precedence 3: Unary operators (right associative)
        operators.put("u-", new Operator(3, false)); // Unary minus
        
        // Precedence 4: Power (right associative for correct evaluation)
        // Example: 2^3^2 = 2^(3^2) = 2^9 = 512, not (2^3)^2 = 64
        operators.put("^", new Operator(4, false));
    }
    
    public boolean isDegreeMode() {
        return isDegreeMode;
    }
    
    public void setDegreeMode(boolean degreeMode) {
        this.isDegreeMode = degreeMode;
    }
    
    /**
     * Toggles between DEG and RAD mode.
     */
    public void toggleAngleMode() {
        isDegreeMode = !isDegreeMode;
    }
    
    /**
     * Returns current angle mode as string for display.
     */
    public String getAngleMode() {
        return isDegreeMode ? "DEG" : "RAD";
    }
    
    public Map<String, Operator> getOperators() {
        return operators;
    }
}