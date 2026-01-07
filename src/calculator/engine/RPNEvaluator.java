package calculator.engine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Evaluates Reverse Polish Notation (postfix) expressions.
 * Uses a stack-based algorithm for efficient evaluation.
 * Handles all mathematical operations using java.lang.Math ONLY.
 * 
 * FILE TYPE: Regular Java Class (Engine component)
 * PURPOSE: Evaluate postfix expressions to get final result
 * 
 * ALGORITHM:
 * 1. Scan tokens left to right
 * 2. Push numbers onto stack
 * 3. When hitting operator: pop operands, compute, push result
 * 4. Final stack value is the answer
 */
public class RPNEvaluator {
    private final MathContext context;
    
    public RPNEvaluator(MathContext context) {
        this.context = context;
    }
    
    /**
     * Evaluates a postfix expression.
     * 
     * @param postfix List of tokens in postfix notation
     * @return The calculated result
     * @throws ArithmeticException for math errors (division by zero, domain errors)
     * 
     * EXAMPLE:
     * Input:  ["3", "4", "2", "*", "+"]
     * Stack:  [3] → [3,4] → [3,4,2] → [3,8] → [11]
     * Output: 11.0
     */
    public double evaluate(List<String> postfix) {
        Deque<Double> valueStack = new ArrayDeque<>();
        
        for (String token : postfix) {
            // Push numbers to stack
            if (isNumber(token)) {
                valueStack.push(Double.parseDouble(token));
            }
            // Push constants to stack
            else if (isConstant(token)) {
                valueStack.push(getConstantValue(token));
            }
            // Apply functions (unary operations)
            else if (isFunction(token)) {
                if (valueStack.isEmpty()) {
                    throw new ArithmeticException("Syntax Error");
                }
                double operand = valueStack.pop();
                double result = applyFunction(token, operand);
                valueStack.push(result);
            }
            // Apply operators
            else if (isOperator(token)) {
                if (token.equals("u-")) {
                    // Unary minus: negate top of stack
                    if (valueStack.isEmpty()) {
                        throw new ArithmeticException("Syntax Error");
                    }
                    double operand = valueStack.pop();
                    valueStack.push(-operand);
                } else {
                    // Binary operators: need 2 operands
                    if (valueStack.size() < 2) {
                        throw new ArithmeticException("Syntax Error");
                    }
                    double right = valueStack.pop();
                    double left = valueStack.pop();
                    double result = applyOperator(token, left, right);
                    valueStack.push(result);
                }
            }
        }
        
        // Final check: stack should have exactly 1 value
        if (valueStack.size() != 1) {
            throw new ArithmeticException("Syntax Error");
        }
        
        return valueStack.pop();
    }
    
    /**
     * Applies a binary operator to two operands.
     * 
     * USES java.lang.Math ONLY - NO CUSTOM IMPLEMENTATIONS
     */
    private double applyOperator(String operator, double left, double right) {
        switch (operator) {
            case "+": return left + right;
            case "-": return left - right;
            case "*": return left * right;
            case "/":
                if (right == 0) throw new ArithmeticException("Division by zero");
                return left / right;
            case "%":
                if (right == 0) throw new ArithmeticException("Division by zero");
                return left % right;
            case "^": 
                return Math.pow(left, right); // Uses java.lang.Math
            default: 
                throw new ArithmeticException("Unknown operator: " + operator);
        }
    }
    
    /**
     * Applies a function to an operand.
     * Handles angle mode conversion for trigonometric functions.
     * 
     * ANGLE MODE HANDLING:
     * - For trig functions (sin, cos, tan): convert input to radians if in DEG mode
     * - For inverse trig (asin, acos, atan): convert output to degrees if in DEG mode
     * 
     * USES java.lang.Math ONLY - NO CUSTOM IMPLEMENTATIONS
     */
    private double applyFunction(String function, double operand) {
        switch (function) {
            // Trigonometric functions (input conversion)
            case "sin":
                return Math.sin(toRadians(operand));
            case "cos":
                return Math.cos(toRadians(operand));
            case "tan":
                return Math.tan(toRadians(operand));
            
            // Inverse trigonometric functions (output conversion)
            case "asin":
                if (operand < -1 || operand > 1) {
                    throw new ArithmeticException("Domain error: asin");
                }
                return toDegrees(Math.asin(operand));
            case "acos":
                if (operand < -1 || operand > 1) {
                    throw new ArithmeticException("Domain error: acos");
                }
                return toDegrees(Math.acos(operand));
            case "atan":
                return toDegrees(Math.atan(operand));
            
            // Logarithmic functions
            case "log":
                if (operand <= 0) {
                    throw new ArithmeticException("Domain error: log");
                }
                return Math.log10(operand); // Base 10
            case "ln":
                if (operand <= 0) {
                    throw new ArithmeticException("Domain error: ln");
                }
                return Math.log(operand); // Natural log
            
            // Exponential and roots
            case "exp":
                return Math.exp(operand); // e^x
            case "sqrt":
                if (operand < 0) {
                    throw new ArithmeticException("Domain error: sqrt");
                }
                return Math.sqrt(operand);
            case "abs":
                return Math.abs(operand);
            
            default:
                throw new ArithmeticException("Unknown function: " + function);
        }
    }
    
    /**
     * Converts angle to radians if in degree mode.
     * Used for trig function INPUTS.
     */
    private double toRadians(double angle) {
        return context.isDegreeMode() ? Math.toRadians(angle) : angle;
    }
    
    /**
     * Converts angle to degrees if in degree mode.
     * Used for inverse trig function OUTPUTS.
     */
    private double toDegrees(double angle) {
        return context.isDegreeMode() ? Math.toDegrees(angle) : angle;
    }
    
    /**
     * Gets the numeric value of a constant.
     * Uses Math.PI and Math.E from java.lang.Math.
     */
    private double getConstantValue(String constant) {
        switch (constant) {
            case "π": return Math.PI;
            case "e": return Math.E;
            default: throw new ArithmeticException("Unknown constant: " + constant);
        }
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