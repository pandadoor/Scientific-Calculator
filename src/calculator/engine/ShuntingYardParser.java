package calculator.engine;

import calculator.model.Operator;
import java.util.*;

/**
 * Implements Dijkstra's Shunting Yard algorithm.
 * Converts infix notation to postfix (RPN) notation.
 * Handles operator precedence, associativity, and functions.
 * 
 * PURPOSE: Parse infix expressions into postfix for easy evaluation
 * 
 * ALGORITHM FLOW:
 * Infix: "3 + 4 * 2" → Postfix: "3 4 2 * +"
 */
public class ShuntingYardParser {
    private final MathContext context;
    
    public ShuntingYardParser(MathContext context) {
        this.context = context;
    }
    
    /**
     * Converts infix tokens to postfix (RPN) notation.
     * 
     * @param tokens List of infix tokens from Tokenizer
     * @return List of postfix tokens ready for evaluation
     * @throws IllegalArgumentException for syntax errors
     * 
     * EXAMPLE:
     * Input:  ["3", "+", "4", "*", "2"]
     * Output: ["3", "4", "2", "*", "+"]
     */
    public List<String> toPostfix(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> operatorStack = new ArrayDeque<>();
        
        // Validate parentheses before processing
        validateParentheses(tokens);
        
        // Handle unary operators (convert "-" to "u-" where appropriate)
        tokens = handleUnaryOperators(tokens);
        
        // Handle absolute value bars (convert |x| to abs(x))
        tokens = handleAbsoluteValue(tokens);
        
        // Main Shunting Yard algorithm
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            
            // Numbers and constants go directly to output queue
            if (isNumber(token) || isConstant(token)) {
                output.add(token);
            }
            // Functions go to operator stack
            else if (isFunction(token)) {
                operatorStack.push(token);
            }
            // Left parenthesis goes to operator stack
            else if (token.equals("(")) {
                operatorStack.push(token);
            }
            // Right parenthesis: pop until left parenthesis
            else if (token.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    output.add(operatorStack.pop());
                }
                if (operatorStack.isEmpty()) {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
                operatorStack.pop(); // Remove left parenthesis
                
                // If there's a function on top, pop it to output
                if (!operatorStack.isEmpty() && isFunction(operatorStack.peek())) {
                    output.add(operatorStack.pop());
                }
            }
            // Operators
            else if (isOperator(token)) {
                Operator currentOp = context.getOperators().get(token);
                
                // Pop operators with higher or equal precedence (considering associativity)
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    String topToken = operatorStack.peek();
                    if (!isOperator(topToken)) break;
                    
                    Operator topOp = context.getOperators().get(topToken);
                    
                    // Pop if:
                    // - Top has higher precedence, OR
                    // - Equal precedence with left associativity
                    if (topOp.getPrecedence() > currentOp.getPrecedence() ||
                        (topOp.getPrecedence() == currentOp.getPrecedence() && currentOp.isLeftAssociative())) {
                        output.add(operatorStack.pop());
                    } else {
                        break;
                    }
                }
                
                operatorStack.push(token);
            }
        }
        
        // Pop remaining operators to output
        while (!operatorStack.isEmpty()) {
            String op = operatorStack.pop();
            if (op.equals("(") || op.equals(")")) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            output.add(op);
        }
        
        return output;
    }
    
    /**
     * Validates that parentheses are balanced.
     * Throws exception if mismatched.
     */
    private void validateParentheses(List<String> tokens) {
        int count = 0;
        for (String token : tokens) {
            if (token.equals("(")) count++;
            if (token.equals(")")) count--;
            if (count < 0) throw new IllegalArgumentException("Mismatched parentheses");
        }
        if (count != 0) throw new IllegalArgumentException("Mismatched parentheses");
    }
    
    /**
     * Converts unary minus to special token "u-".
     * 
     * DETECTION RULES:
     * - At beginning of expression: "-5" → ["u-", "5"]
     * - After an operator: "3 + -5" → ["3", "+", "u-", "5"]
     * - After opening parenthesis: "(-5)" → ["(", "u-", "5", ")"]
     */
    private List<String> handleUnaryOperators(List<String> tokens) {
        List<String> result = new ArrayList<>();
        
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            
            // Check if minus is unary
            if (token.equals("-")) {
                boolean isUnary = (i == 0) || 
                                 isOperator(tokens.get(i - 1)) ||
                                 tokens.get(i - 1).equals("(");
                
                if (isUnary) {
                    result.add("u-"); // Special unary minus token
                } else {
                    result.add(token); // Binary minus
                }
            } else {
                result.add(token);
            }
        }
        
        return result;
    }
    
    /**
     * Converts absolute value bars |x| to abs(x) function notation.
     * 
     * EXAMPLE:
     * "|", "5", "|" → "abs", "(", "5", ")"
     */
    private List<String> handleAbsoluteValue(List<String> tokens) {
        List<String> result = new ArrayList<>();
        boolean inAbsolute = false;
        
        for (String token : tokens) {
            if (token.equals("|")) {
                if (!inAbsolute) {
                    // Opening bar: convert to abs(
                    result.add("abs");
                    result.add("(");
                    inAbsolute = true;
                } else {
                    // Closing bar: convert to )
                    result.add(")");
                    inAbsolute = false;
                }
            } else {
                result.add(token);
            }
        }
        
        return result;
    }
    
    private boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isConstant(String token) {
        return token.equals("π") || token.equals("e");
    }
    
    private boolean isFunction(String token) {
        return token.matches("sin|cos|tan|asin|acos|atan|log|ln|sqrt|abs|exp");
    }
    
    private boolean isOperator(String token) {
        return context.getOperators().containsKey(token);
    }
}