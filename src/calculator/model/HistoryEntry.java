package calculator.model;

import java.io.Serializable;

/**
 * Represents a single calculation in the history buffer.
 * Stores both the expression and its result for later retrieval.
 * 
 * FILE TYPE: Regular Java Class (POJO)
 * PURPOSE: Data model for history entries
 * 
 * Implements Serializable for potential future persistence to file.
 */
public class HistoryEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String expression;
    private final String result;
    
    /**
     * Creates a history entry.
     * 
     * @param expression The mathematical expression (e.g., "2+2")
     * @param result The calculated result (e.g., "4")
     */
    public HistoryEntry(String expression, String result) {
        this.expression = expression;
        this.result = result;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public String getResult() {
        return result;
    }
    
    /**
     * Returns formatted string for display in history list.
     * Format: "expression = result"
     */
    @Override
    public String toString() {
        return expression + " = " + result;
    }
}