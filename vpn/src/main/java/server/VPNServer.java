package server;

import Security.SSLUtils;
import ui.VPNServerGUI;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VPNServer {
    private static final int PORT = 4433;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private static ConcurrentHashMap<String, SSLSocket> connectedClients = new ConcurrentHashMap<>();
    private static VPNServerGUI gui;

    public static void main(String[] args) {
        gui = new VPNServerGUI(); // Create GUI instance
        try {
            SSLServerSocketFactory sslServerSocketFactory = SSLUtils.getSSLServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(PORT);
            gui.log("VPN Server started on port " + PORT);

            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().toString();
                connectedClients.put(clientAddress, clientSocket);
                gui.log("Client connected: " + clientAddress);
                updateServerGUI(); // Call GUI update
                threadPool.execute(new ClientHandler(clientSocket, clientAddress));
            }
        } catch (Exception e) {
            gui.log("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateServerGUI() {
        gui.updateClients(Collections.list(connectedClients.keys()));
    }

    static class ClientHandler implements Runnable {
        private SSLSocket clientSocket;
        private String clientAddress;

        public ClientHandler(SSLSocket clientSocket, String clientAddress) {
            this.clientSocket = clientSocket;
            this.clientAddress = clientAddress;
        }

        @Override
        public void run() {
            try {
                clientSocket.getInputStream().read(); // Simulate client handling
            } catch (IOException e) {
                gui.log("Client disconnected: " + clientAddress);
                connectedClients.remove(clientAddress);
                updateServerGUI();
            }
        }
    }
}
