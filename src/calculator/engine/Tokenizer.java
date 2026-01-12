package calculator.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes mathematical expressions into discrete tokens.
 * Handles implicit multiplication (2π, 2(3+4), sin30).
 * Sanitizes input by removing commas and normalizing whitespace.
 * 
 * PURPOSE: Convert string input into parseable tokens
 * PIPELINE: User Input → sanitize() → tokenize() → Parser
 */
public class Tokenizer {
    // Regex patterns for token recognition
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("sin|cos|tan|asin|acos|atan|log|ln|sqrt|abs|exp");
    private static final Pattern CONSTANT_PATTERN = Pattern.compile("π|e");
    
    /**
     * Sanitizes input by removing commas and extra whitespace.
     * CRITICAL: This prevents "1,235" from being treated as text.
     * 
     * @param input Raw user input
     * @return Cleaned input string
     * 
     * EXAMPLES:
     * "1,235.50" → "1235.50"
     * "  2 + 2  " → "2 + 2"
     */
    public static String sanitize(String input) {
        if (input == null) return "";
        return input.replace(",", "").trim();
    }
    
    /**
     * Tokenizes the expression into a list of string tokens.
     * Handles implicit multiplication insertion.
     * 
     * @param expression The sanitized expression
     * @return List of tokens ready for parsing
     * @throws IllegalArgumentException if expression contains invalid characters
     * 
     * EXAMPLES:
     * "2π" → ["2", "*", "π"]
     * "sin30" → ["sin", "*", "30"]  // Wrong! Should be ["sin", "(", "30", ")"]
     * Actually: "sin30" → ["sin", "30"] (function takes argument)
     */
    public static List<String> tokenize(String expression) {
        expression = sanitize(expression);
        List<String> tokens = new ArrayList<>();
        int i = 0;
        
        while (i < expression.length()) {
            char c = expression.charAt(i);
            
            // Skip whitespace
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            
            // Check for numbers (including decimals)
            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expression.length() && 
                       (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i));
                    i++;
                }
                
                // Insert implicit multiplication before number if needed
                if (!tokens.isEmpty() && needsImplicitMultiplication(tokens.get(tokens.size() - 1), num.toString())) {
                    tokens.add("*");
                }
                
                tokens.add(num.toString());
                continue;
            }
            
            // Check for functions (sin, cos, tan, etc.)
            String remaining = expression.substring(i);
            Matcher funcMatcher = FUNCTION_PATTERN.matcher(remaining);
            if (funcMatcher.lookingAt()) {
                String func = funcMatcher.group();
                
                // Insert implicit multiplication before function if needed
                if (!tokens.isEmpty() && needsImplicitMultiplication(tokens.get(tokens.size() - 1), func)) {
                    tokens.add("*");
                }
                
                tokens.add(func);
                i += func.length();
                continue;
            }
            
            // Check for constants (π, e)
            Matcher constMatcher = CONSTANT_PATTERN.matcher(remaining);
            if (constMatcher.lookingAt()) {
                String constant = constMatcher.group();
                
                // Insert implicit multiplication before constant if needed
                if (!tokens.isEmpty() && needsImplicitMultiplication(tokens.get(tokens.size() - 1), constant)) {
                    tokens.add("*");
                }
                
                tokens.add(constant);
                i += constant.length();
                continue;
            }
            
            // Check for operators and parentheses
            if ("+-*/^%()".indexOf(c) != -1) {
                String token = String.valueOf(c);
                
                // Insert implicit multiplication after closing parenthesis if needed
                if (c == '(' && !tokens.isEmpty() && needsImplicitMultiplication(tokens.get(tokens.size() - 1), token)) {
                    tokens.add("*");
                }
                
                tokens.add(token);
                i++;
                continue;
            }
            
            // Check for absolute value bars
            if (c == '|') {
                tokens.add("|");
                i++;
                continue;
            }
            
            throw new IllegalArgumentException("Invalid character: " + c);
        }
        
        return tokens;
    }
    
    /**
     * Determines if implicit multiplication should be inserted between two tokens.
     * 
     * RULES FOR IMPLICIT MULTIPLICATION:
     * - number + constant: 2π → 2*π
     * - number + (: 2(3) → 2*(3)
     * - ) + number: (3)2 → (3)*2
     * - ) + (: (3)(4) → (3)*(4)
     * - constant + (: π(3) → π*(3)
     * - number + function: 2sin(30) → 2*sin(30)
     */
    private static boolean needsImplicitMultiplication(String prevToken, String currentToken) {
        if (prevToken == null || currentToken == null) return false;
        
        boolean prevIsNumber = NUMBER_PATTERN.matcher(prevToken).matches();
        boolean prevIsConstant = CONSTANT_PATTERN.matcher(prevToken).matches();
        boolean prevIsCloseParen = prevToken.equals(")");
        
        boolean currIsNumber = NUMBER_PATTERN.matcher(currentToken).matches();
        boolean currIsConstant = CONSTANT_PATTERN.matcher(currentToken).matches();
        boolean currIsOpenParen = currentToken.equals("(");
        boolean currIsFunction = FUNCTION_PATTERN.matcher(currentToken).matches();
        
        // Number or constant before constant
        if ((prevIsNumber || prevIsConstant) && currIsConstant) return true;
        
        // Number, constant, or ) before (
        if ((prevIsNumber || prevIsConstant || prevIsCloseParen) && currIsOpenParen) return true;
        
        // ) before number or constant
        if (prevIsCloseParen && (currIsNumber || currIsConstant)) return true;
        
        // Number or constant before function
        if ((prevIsNumber || prevIsConstant) && currIsFunction) return true;
        
        return false;
    }
}