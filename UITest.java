import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

public class UITest {
    public static void main(String[] args) {
        // 1. Apply the Modern Dark Theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf. Make sure the JAR is in your build path!");
        }

        // 2. Build the Main Window
        JFrame frame = new JFrame("☕ University Attendance System");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 3. The Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(33, 33, 33)); // Dark gray
        JLabel title = new JLabel("SCAN STUDENT ID");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerPanel.add(title);
        frame.add(headerPanel, BorderLayout.NORTH);

        // 4. The Center Screen (The "Ghost Scanner" view)
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.setBackground(new Color(45, 45, 45)); // Slightly lighter dark gray
        
        JLabel nameLabel = new JLabel("Waiting for Scan...", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 45));
        nameLabel.setForeground(Color.LIGHT_GRAY);
        
        // You can change this text to test how "✅ Welcome to CC103" looks!
        JLabel statusLabel = new JLabel("-", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        statusLabel.setForeground(new Color(76, 175, 80)); // Success Green
        
        centerPanel.add(nameLabel);
        centerPanel.add(statusLabel);
        frame.add(centerPanel, BorderLayout.CENTER);

        // 5. Display the App
        frame.setLocationRelativeTo(null); // Centers it on your monitor
        frame.setVisible(true);
    }
}
