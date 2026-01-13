package calculator;

import calculator.ui.CalculatorFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try {
            FlatDarkLaf.setup();
            System.out.println("FlatLaf Dark theme loaded successfully!");
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf theme");
            e.printStackTrace();
        }
        
        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            CalculatorFrame frame = new CalculatorFrame();
            frame.setVisible(true);
        });
    }
}