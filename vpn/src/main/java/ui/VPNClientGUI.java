// VPNClientGUI.java (UI Package)
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
    private JButton connectButton, disconnectButton;

    public VPNClientGUI(VPNClient client) {
        this.client = client;
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("VPN Client");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        frame.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);

        buttonPanel.add(connectButton);
        buttonPanel.add(disconnectButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.connectToServer();
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
    }
}
