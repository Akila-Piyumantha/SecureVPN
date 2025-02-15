package ui;

import client.VPNClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VPNClientGUI {
    private final VPNClient client;
    private JFrame frame;
    private JTextArea statusArea;
    private JTextField urlField;
    private JButton connectButton, disconnectButton;

    public VPNClientGUI(VPNClient client) {
        this.client = client;
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("VPN Client");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());

        // Modern dark theme colors
        Color backgroundColor = new Color(45, 45, 45);
        Color textColor = new Color(200, 200, 200);
        Color buttonColor = new Color(70, 130, 180);
        Color connectColor = new Color(46, 204, 113);
        Color disconnectColor = new Color(231, 76, 60);
        Color borderColor = new Color(100, 100, 100);

        frame.getContentPane().setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel urlLabel = new JLabel("Server URL");
        urlLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        frame.add(urlLabel, gbc);

        urlField = new JTextField("127.0.0.1:4433");
        urlField.setPreferredSize(new Dimension(250, 30));
        urlField.setBackground(new Color(60, 60, 60));
        urlField.setForeground(textColor);
        urlField.setBorder(BorderFactory.createLineBorder(borderColor));
        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(urlField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(backgroundColor);

        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);

        connectButton.setPreferredSize(new Dimension(120, 40));
        disconnectButton.setPreferredSize(new Dimension(120, 40));

        // Style buttons
        connectButton.setBackground(connectColor);
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);

        disconnectButton.setBackground(disconnectColor);
        disconnectButton.setForeground(Color.WHITE);
        disconnectButton.setFocusPainted(false);

        buttonPanel.add(connectButton);
        buttonPanel.add(disconnectButton);
        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(buttonPanel, gbc);

        JLabel statusLabel = new JLabel("Connection Status");
        statusLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 3;
        frame.add(statusLabel, gbc);

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Arial", Font.PLAIN, 14));
        statusArea.setBackground(new Color(60, 60, 60));
        statusArea.setForeground(textColor);
        statusArea.setBorder(BorderFactory.createLineBorder(borderColor));

        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setPreferredSize(new Dimension(400, 100));
        gbc.gridx = 0;
        gbc.gridy = 4;
        frame.add(scrollPane, gbc);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = urlField.getText().trim();
                if (!url.isEmpty()) {
                    try {
                        String[] parts = url.split(":"); // Splitting address and port
                        String serverAddress = parts[0];
                        int serverPort = (parts.length > 1) ? Integer.parseInt(parts[1]) : 4433; // Default port 4433
                        client.connectToServer(serverAddress, serverPort);
                    } catch (NumberFormatException ex) {
                        updateStatus("Invalid port number.");
                    }
                } else {
                    updateStatus("Please enter a server URL.");
                }
            }
        });

        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.disconnectFromServer();
            }
        });

        frame.setVisible(true);
    }

    public void updateStatus(String message) {
        statusArea.append(message + "\n");
    }

    public void setConnectionState(boolean isConnected) {
        connectButton.setEnabled(!isConnected);
        disconnectButton.setEnabled(isConnected);
        urlField.setEnabled(!isConnected);
    }
}
