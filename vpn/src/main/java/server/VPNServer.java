package server;

import Security.SSLUtils;
import ui.VPNServerGUI;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.BindException;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VPNServer {
    private int SERVER_PORT = 4433;
    private ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private ConcurrentHashMap<String, SSLSocket> connectedClients = new ConcurrentHashMap<>();
    private VPNServerGUI gui;
    private SSLServerSocket serverSocket;
    private boolean isRunning = false;

    public VPNServer() {
        gui = new VPNServerGUI(this); // Pass instance to GUI
    }

    public void startServer() {
        if (isRunning) {
            gui.log("Server is already running!");
            return;
        }

        new Thread(() -> {
            try {
                SSLServerSocketFactory sslServerSocketFactory = SSLUtils.getSSLServerSocketFactory();
                try {
                    serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(SERVER_PORT);
                } catch (BindException e) {
                    gui.log("Port " + SERVER_PORT + " is in use! Trying another port...");
                    SERVER_PORT += 1;
                    serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(SERVER_PORT);
                }

                isRunning = true;
                gui.log("VPN Server started on port " + SERVER_PORT);

                while (isRunning) {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    String clientAddress = clientSocket.getInetAddress().toString();
                    connectedClients.put(clientAddress, clientSocket);
                    gui.log("Client connected: " + clientAddress);
                    updateServerGUI();
                    threadPool.execute(new ClientHandler(clientSocket, clientAddress, this)); // Pass VPNServer instance
                }
            } catch (Exception e) {
                gui.log("Error: " + e.getMessage());
            }
        }).start();
    }

    public void stopServer() {
        if (!isRunning) {
            gui.log("Server is not running!");
            return;
        }

        try {
            isRunning = false;

            // Close all client connections
            for (SSLSocket client : connectedClients.values()) {
                client.close();
            }
            connectedClients.clear();
            updateServerGUI();

            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            gui.log("VPN Server stopped.");
        } catch (IOException e) {
            gui.log("Error stopping server: " + e.getMessage());
        }
    }

    private void updateServerGUI() {
        gui.updateClients(Collections.list(connectedClients.keys()));
    }

    static class ClientHandler implements Runnable {
        private SSLSocket clientSocket;
        private String clientAddress;
        private VPNServer serverInstance; // Store VPNServer instance

        public ClientHandler(SSLSocket clientSocket, String clientAddress, VPNServer serverInstance) {
            this.clientSocket = clientSocket;
            this.clientAddress = clientAddress;
            this.serverInstance = serverInstance;
        }

        @Override
        public void run() {
            try {
                clientSocket.getInputStream().read(); // Simulate client handling
            } catch (IOException e) {
                serverInstance.gui.log("Client disconnected: " + clientAddress);
                serverInstance.connectedClients.remove(clientAddress);
                serverInstance.updateServerGUI();
            }
        }
    }

    public static void main(String[] args) {
        new VPNServer(); // Start VPN Server GUI
    }
}
