package ui;

import server.VPNServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class VPNServerGUI extends JFrame {
    private JTextArea logArea;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;
    private JTextField serverStatusField;
    private JButton startServerButton, stopServerButton;
    private VPNServer server;

    public VPNServerGUI(VPNServer server) {
        this.server = server;
        setTitle("VPN Server");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        // Modern dark theme colors
        Color backgroundColor = new Color(45, 45, 45);
        Color textColor = new Color(200, 200, 200);
        Color connectColor = new Color(46, 204, 113);
        Color disconnectColor = new Color(231, 76, 60);
        Color borderColor = new Color(100, 100, 100);

        getContentPane().setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel serverStatusLabel = new JLabel("Server Status");
        serverStatusLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(serverStatusLabel, gbc);

        serverStatusField = new JTextField("Stopped");
        serverStatusField.setEditable(false);
        serverStatusField.setPreferredSize(new Dimension(250, 30));
        serverStatusField.setBackground(new Color(60, 60, 60));
        serverStatusField.setForeground(textColor);
        serverStatusField.setBorder(BorderFactory.createLineBorder(borderColor));
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(serverStatusField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(backgroundColor);

        startServerButton = new JButton("Start Server");
        stopServerButton = new JButton("Stop Server");
        stopServerButton.setEnabled(false);

        startServerButton.setPreferredSize(new Dimension(120, 40));
        stopServerButton.setPreferredSize(new Dimension(120, 40));

        // Style buttons
        startServerButton.setBackground(connectColor);
        startServerButton.setForeground(Color.WHITE);
        startServerButton.setFocusPainted(false);

        stopServerButton.setBackground(disconnectColor);
        stopServerButton.setForeground(Color.WHITE);
        stopServerButton.setFocusPainted(false);

        buttonPanel.add(startServerButton);
        buttonPanel.add(stopServerButton);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(buttonPanel, gbc);

        JLabel clientLabel = new JLabel("Connected Clients");
        clientLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(clientLabel, gbc);

        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setBackground(new Color(60, 60, 60));
        clientList.setForeground(textColor);
        clientList.setBorder(BorderFactory.createLineBorder(borderColor));

        JScrollPane clientScrollPane = new JScrollPane(clientList);
        clientScrollPane.setPreferredSize(new Dimension(400, 100));
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(clientScrollPane, gbc);

        JLabel logLabel = new JLabel("Server Logs");
        logLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(logLabel, gbc);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Arial", Font.PLAIN, 14));
        logArea.setBackground(new Color(60, 60, 60));
        logArea.setForeground(textColor);
        logArea.setBorder(BorderFactory.createLineBorder(borderColor));

        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(400, 100));
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(logScrollPane, gbc);

        // Action Listeners for Buttons
        startServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.startServer();
                serverStatusField.setText("Running");
                startServerButton.setEnabled(false);
                stopServerButton.setEnabled(true);
            }
        });

        stopServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.stopServer();
                serverStatusField.setText("Stopped");
                startServerButton.setEnabled(true);
                stopServerButton.setEnabled(false);
            }
        });

        setVisible(true);
    }

    public void updateClients(List<String> clients) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.clear();
            for (String client : clients) {
                clientListModel.addElement(client);
            }
        });
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
}
