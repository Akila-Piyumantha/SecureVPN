package ui;
import client.VPNClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VPNClientGUI extends JFrame {
    private VPNClient vpnClient;
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;

    public VPNClientGUI() {
        // Set up the window
        setTitle("VPN Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create a text area for logging
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        startButton = new JButton("Start VPN");
        stopButton = new JButton("Stop VPN");

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Create the VPNClient instance
        vpnClient = new VPNClient(this);

        // Button listeners
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vpnClient.startClient();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vpnClient.stopClient();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });

        stopButton.setEnabled(false);  // Start with stop disabled
    }

    // Method to log messages in the GUI
    public void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());  // Scroll to the bottom
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VPNClientGUI ui = new VPNClientGUI();
            ui.setVisible(true);
        });
    }
}
