package server;
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

class clientHandler implements Runnable {
    private SSLSocket clientSocket;

    public clientHandler(SSLSocket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            String clientMsg;
            while ((clientMsg = in.readLine()) != null) {
                System.out.println("Received request to: " + clientMsg);
             //   String decryptedRequest = EncryptionUtils.decrypt(clientMsg);
                String response = PacketForwarder.forwardRequest(clientMsg);
              //  String encryptedResponse = EncryptionUtils.encrypt(response);
                out.write(response + "\n");
                System.out.println(response);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
