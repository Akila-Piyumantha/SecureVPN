package server;

import Security.SSLUtils;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import ui.VPNServerGUI;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class VPNServer {
    private static final int PORT = 4433;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private static ConcurrentHashMap<String, SSLSocket> connectedClients = new ConcurrentHashMap<>();
    private static VPNServerGUI gui;

    private static PcapHandle tapHandle; // Handle for TAP interface

    public static void main(String[] args) {
        gui = new VPNServerGUI();

        try {
            // Initialize TAP interface using Pcap4J or any similar library
            tapHandle = Pcaps.openLive("tap0", 65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);

            SSLServerSocketFactory sslServerSocketFactory = SSLUtils.getSSLServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(PORT);
            gui.log("VPN Server started on port " + PORT);

            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().toString();
                connectedClients.put(clientAddress, clientSocket);
                gui.log("Client connected: " + clientAddress);

                // Start a new thread for each client to handle packet forwarding
                threadPool.execute(new ClientHandler(clientSocket, clientAddress));
            }
        } catch (Exception e) {
            gui.log("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private SSLSocket clientSocket;
        private String clientAddress;
        private static final int BUFFER_SIZE = 4096;

        public ClientHandler(SSLSocket clientSocket, String clientAddress) {
            this.clientSocket = clientSocket;
            this.clientAddress = clientAddress;
        }

        @Override
        public void run() {
            try {
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();

                // Forward data packets between the client and TAP interface
                while (true) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead = in.read(buffer);

                    // If end of stream is reached, client disconnected
                    if (bytesRead == -1) break;

                    // Forward the packet to the TAP interface
                    sendPacketToTap(buffer, bytesRead);

                    // You can add more logic to send the response back to the client
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                gui.log("Client disconnected: " + clientAddress);
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ignored) {}
                connectedClients.remove(clientAddress);
            }
        }

        private static void sendPacketToTap(byte[] buffer, int bytesRead) {
            try {
                if (tapHandle != null) {
                    // Send data to the TAP interface
                    tapHandle.sendPacket(new RawPacket(buffer, 0, bytesRead));
                }
            } catch (PcapNativeException | NotOpenException e) {
                e.printStackTrace();
            }
        }
    }
}



