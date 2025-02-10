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
        responseArea.setText("Fetching data...");  // Show loading message

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    SSLSocketFactory factory = ClientSSLUtils.getSSLSocketFactory();
                    SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 4433);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    // Get the user-entered URL
                    String url = urlField.getText();
                    out.write(url + "\n");
                    out.flush();

                    String responseLine;
                    StringBuilder responseBuilder = new StringBuilder();

                    // Read server response line by line
                    while ((responseLine = in.readLine()) != null) {
                        responseBuilder.append(responseLine).append("\n");
                        publish(responseLine);  // Update UI progressively
                    }

                    socket.close();
                    return null;
                } catch (Exception e) {
                    publish("Error: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String line : chunks) {
                    responseArea.append(line + "\n");  // Update UI with new data
                }
            }
        };

        worker.execute();
    }

}
