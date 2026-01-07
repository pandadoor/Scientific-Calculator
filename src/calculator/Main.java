package calculator;

import calculator.ui.CalculatorFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.SwingUtilities;

/**
 * Main entry point for the Scientific Calculator application.
 * Initializes FlatLaf theme and launches the UI.
 * 
 * INSTRUCTIONS:
 * - This is a regular Java Class with main method
 * - Right-click → Run As → Java Application
 */
public class Main {
    public static void main(String[] args) {
        // Set FlatLaf Dark theme before creating UI
        try {
            FlatDarkLaf.setup();
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