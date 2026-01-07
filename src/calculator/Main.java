package calculator;

import calculator.ui.CalculatorFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.SwingUtilities;

/**
 * Main entry point for the Scientific Calculator application.
 * Initializes FlatLaf Dark theme (FIXED - no theme switching).
 * 
 * INSTRUCTIONS:
 * - This is a regular Java Class with main method
 * - Right-click → Run As → Java Application
 */
public class Main {
    public static void main(String[] args) {
        // Set FlatLaf Dark theme - FIXED THEME (no switching)
        try {
            FlatDarkLaf.setup();
            System.out.println("FlatLaf Dark theme loaded successfully!");
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf theme");
            e.printStackTrace();
        }
        
        // Launch UI on Event Dispatch Thread (Swing best practice)
        SwingUtilities.invokeLater(() -> {
            CalculatorFrame frame = new CalculatorFrame();
            frame.setVisible(true);
        });
    }
}