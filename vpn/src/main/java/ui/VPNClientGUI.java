package ui;
import Security.SSLUtils;
import client.EncryptionUtils;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;
import java.util.concurrent.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

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
            SSLSocketFactory factory = SSLUtils.getSSLSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 4433);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String url = EncryptionUtils.encrypt(urlField.getText());
            out.write(url + "\n");
            out.flush();

            responseArea.setText("");
            String response;
            while ((response = in.readLine()) != null) {
                responseArea.append(EncryptionUtils.decrypt(response) + "\n");
            }
            socket.close();
        } catch (Exception e) {
            responseArea.setText("Error: " + e.getMessage());
        }
    }
}
