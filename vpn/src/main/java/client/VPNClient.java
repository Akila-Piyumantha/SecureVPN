package client;
import ui.VPNClientGUI;

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

class VPNClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4433;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VPNClientGUI());
    }
}
