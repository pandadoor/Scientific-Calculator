package calculator.model;

/**
 * Represents an operator with its precedence and associativity.
 * Used by the Shunting Yard algorithm to correctly parse expressions.
 * 
 * PURPOSE: Data model for operator properties
 */
public class Operator {
    private final int precedence;
    private final boolean leftAssociative;
    
    /**
     * Creates an operator with specified precedence and associativity.
     * 
     * @param precedence Higher values = higher precedence (1=lowest, 4=highest)
     * @param leftAssociative true for left-to-right evaluation, false for right-to-left
     * 
     * EXAMPLES:
     * - Addition: precedence=1, leftAssociative=true
     * - Power: precedence=4, leftAssociative=false (right associative)
     */
    public Operator(int precedence, boolean leftAssociative) {
        this.precedence = precedence;
        this.leftAssociative = leftAssociative;
    }
    
    public int getPrecedence() {
        return precedence;
    }
    
    public boolean isLeftAssociative() {
        return leftAssociative;
    }
}