import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.time.LocalTime;

public class ProfessionalAttendanceSystem extends JFrame {

    // ==========================================
    // 1. DATABASE CONFIGURATION (SUPABASE)
    // ==========================================
    private static final String DB_URL = "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres";
    private static final String USER = "postgres.uesarxjlyiwyuuvubvex"; 
    private static final String PASS = "YOUR_SUPABASE_PASSWORD"; // ⚠️ Put your password here

    // ==========================================
    // 2. UI COMPONENTS & STATE
    // ==========================================
    private JLabel nameLabel;
    private JLabel statusLabel;
    private final StringBuilder barcodeBuffer = new StringBuilder();
    private final Timer resetTimer; // Clears the screen after scanning

    public ProfessionalAttendanceSystem() {
        // --- Window Setup ---
        setTitle("University Attendance System");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(25, 25, 25));

        // --- Header Panel ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(15, 15, 15));
        JLabel title = new JLabel("SCAN STUDENT ID");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        headerPanel.add(title);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Panel (The Display) ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.setBackground(new Color(25, 25, 25));
        
        nameLabel = new JLabel("Waiting for Scan...", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 50));
        nameLabel.setForeground(Color.LIGHT_GRAY);
        
        statusLabel = new JLabel("-", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        statusLabel.setForeground(new Color(25, 25, 25)); // Hidden initially
        
        centerPanel.add(nameLabel);
        centerPanel.add(statusLabel);
        add(centerPanel, BorderLayout.CENTER);

        // --- Auto-Reset Timer (3 Seconds) ---
        resetTimer = new Timer(3000, e -> resetUI());
        resetTimer.setRepeats(false);

        // --- The "Ghost Scanner" (Global Key Dispatcher) ---
        // This is the professional way to catch keyboard/scanner input globally
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_TYPED) {
                char c = e.getKeyChar();
                if (c == '\n') {
                    // Enter pressed: Process the barcode on a background thread
                    String scannedCode = barcodeBuffer.toString().trim();
                    barcodeBuffer.setLength(0); // Clear buffer
                    
                    if (!scannedCode.isEmpty()) {
                        processScanAsync(scannedCode);
                    }
                } else {
                    barcodeBuffer.append(c);
                }
            }
            return false;
        });
    }

    // ==========================================
    // 3. THE BRAIN (ASYNC DATABASE LOGIC)
    // ==========================================
    private void processScanAsync(String barcode) {
        // Stop the reset timer if it's running, and show "Processing"
        resetTimer.stop();
        updateUI("Processing...", "Checking Database...", Color.LIGHT_GRAY);

        // Run DB calls on a background thread so the UI doesn't freeze
        new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                
                // Step A: Find the Student
                String checkStudentSQL = "SELECT full_name FROM students WHERE barcode = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkStudentSQL);
                checkStmt.setString(1, barcode);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    updateUI("Unknown ID: " + barcode, "❌ Student Not Found", new Color(244, 67, 54)); // Red
                    return;
                }
                String studentName = rs.getString("full_name");

                // Step B: Check for Duplicates Today
                String dupSQL = "SELECT id FROM attendance WHERE student_barcode = ? AND DATE(scan_timestamp) = CURRENT_DATE";
                PreparedStatement dupStmt = conn.prepareStatement(dupSQL);
                dupStmt.setString(1, barcode);
                
                if (dupStmt.executeQuery().next()) {
                    updateUI(studentName, "⚠️ Already Scanned Today", new Color(255, 152, 0)); // Orange
                    return;
                }

                // Step C: Calculate Status (Assuming class starts at 8:00 AM)
                LocalTime classStart = LocalTime.of(8, 0); 
                String status = LocalTime.now().isAfter(classStart) ? "Late" : "Present";
                Color statusColor = status.equals("Present") ? new Color(76, 175, 80) : new Color(255, 193, 7); // Green or Yellow

                // Step D: Save to Supabase
                String insertSQL = "INSERT INTO attendance (student_barcode, status) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                insertStmt.setString(1, barcode);
                insertStmt.setString(2, status);
                insertStmt.executeUpdate();

                // Step E: Display Success
                updateUI(studentName, "✅ Recorded as " + status, statusColor);

            } catch (SQLException e) {
                e.printStackTrace();
                updateUI("System Error", "❌ Database Connection Failed", new Color(244, 67, 54));
            }
        }).start();
    }

    // ==========================================
    // 4. UI HELPERS
    // ==========================================
    private void updateUI(String name, String statusText, Color statusColor) {
        // Swing GUI updates MUST happen on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            nameLabel.setText(name);
            nameLabel.setForeground(Color.WHITE);
            statusLabel.setText(statusText);
            statusLabel.setForeground(statusColor);
            
            // Start the 3-second timer to clear the screen if it's not "Processing"
            if (!name.equals("Processing...")) {
                resetTimer.restart();
            }
        });
    }

    private void resetUI() {
        SwingUtilities.invokeLater(() -> {
            nameLabel.setText("Waiting for Scan...");
            nameLabel.setForeground(Color.LIGHT_GRAY);
            statusLabel.setForeground(new Color(25, 25, 25)); // Hide status text by making it blend with background
        });
    }

    // ==========================================
    // 5. MAIN LAUNCHER
    // ==========================================
    public static void main(String[] args) {
        // Apply the FlatDarkLaf Theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("FlatLaf Library not found!");
        }

        // Launch the App
        SwingUtilities.invokeLater(() -> {
            ProfessionalAttendanceSystem app = new ProfessionalAttendanceSystem();
            app.setLocationRelativeTo(null); // Center on screen
            app.setVisible(true);
        });
    }
}
