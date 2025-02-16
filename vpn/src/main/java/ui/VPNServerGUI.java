package ui;

import server.VPNServer;

import javax.swing.*;
import java.awt.*;

public class VPNServerGUI extends JFrame {
    private JTextArea logArea;
    private JTextField serverStatusField;
    private JButton startServerButton, stopServerButton;
    private VPNServer server;
    private Thread serverThread;

    public VPNServerGUI() {
        server = new VPNServer(this);
        setTitle("VPN Server");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        serverStatusField = new JTextField("Stopped", 20);
        serverStatusField.setEditable(false);
        topPanel.add(new JLabel("Server Status:"));
        topPanel.add(serverStatusField);

        startServerButton = new JButton("Start Server");
        stopServerButton = new JButton("Stop Server");
        stopServerButton.setEnabled(false);
        topPanel.add(startServerButton);
        topPanel.add(stopServerButton);
        add(topPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        startServerButton.addActionListener(e -> {
            serverThread = new Thread(() -> server.startServer());
            serverThread.start();
            serverStatusField.setText("Running");
            startServerButton.setEnabled(false);
            stopServerButton.setEnabled(true);
        });

        stopServerButton.addActionListener(e -> {
            server.stopServer();
            serverStatusField.setText("Stopped");
            startServerButton.setEnabled(true);
            stopServerButton.setEnabled(false);
        });

        setVisible(true);
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VPNServerGUI::new);
    }
}
