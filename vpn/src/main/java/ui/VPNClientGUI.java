package ui;
  // Use the correct SSLUtils import
import client.ClientSSLUtils;
import client.EncryptionUtils;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.util.concurrent.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class VPNClientGUI extends JFrame {
    private JTextField urlField;
    private JTextArea responseArea;

    public VPNClientGUI() {
        setTitle("VPN Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        urlField = new JTextField();
        JButton fetchButton = new JButton("Fetch");
        responseArea = new JTextArea();
        responseArea.setEditable(false);

        fetchButton.addActionListener(e -> fetchURL());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(fetchButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(responseArea), BorderLayout.CENTER);
        setVisible(true);
    }

    private void fetchURL() {
        try {
            // Use the ClientSSLUtils to get the correct SSLSocketFactory for the client
            SSLSocketFactory factory = ClientSSLUtils.getSSLSocketFactory();  // Changed to ClientSSLUtils
            SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 4433);  // Connect to server

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String url = EncryptionUtils.encrypt(urlField.getText());  // Encrypt URL
            out.write(url + "\n");
            out.flush();

            responseArea.setText("");  // Clear previous response
            String response;
            while ((response = in.readLine()) != null) {
                responseArea.append(EncryptionUtils.decrypt(response) + "\n");  // Decrypt server response
            }
            socket.close();  // Close the socket connection
        } catch (Exception e) {
            responseArea.setText("Error: " + e.getMessage());  // Display error if any
        }
    }
}
