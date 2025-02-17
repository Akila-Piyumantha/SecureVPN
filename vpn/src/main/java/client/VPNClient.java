package client;

import ui.VPNClientGUI;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;

public class VPNClient {
    private static final int LOCAL_PROXY_PORT = 3128;
    private static final String VPN_SERVER_IP = "localhost";
    private static final int VPN_SERVER_PORT = 443;
    private VPNClientGUI ui;
    private boolean running = false;

    private ServerSocket proxyServer; // Declare as instance variable
    private Socket vpnSocket;         // Declare as instance variable

    public VPNClient(VPNClientGUI ui) {
        this.ui = ui;
    }

    private void connectToVPNServer() {
        try {
            vpnSocket = new Socket(VPN_SERVER_IP, VPN_SERVER_PORT);
            ui.log("🌐 Connected to VPN Server at " + VPN_SERVER_IP + ":" + VPN_SERVER_PORT);

            BufferedReader vpnIn = new BufferedReader(new InputStreamReader(vpnSocket.getInputStream()));
            PrintWriter vpnOut = new PrintWriter(vpnSocket.getOutputStream(), true);

            // Optional: Send a handshake message
            vpnOut.println("HELLO VPN SERVER");

            // Read server responses (infinite loop to maintain connection)
            String serverResponse;
            while ((serverResponse = vpnIn.readLine()) != null && running) {
                ui.log("💬 VPN Server: " + serverResponse);
            }
        } catch (IOException e) {
            ui.log("❌ Failed to connect to VPN server: " + e.getMessage());
        }
    }

    public void startClient() {
        running = true;

        // Connect to VPN Server when the client starts
        new Thread(this::connectToVPNServer).start();

        // Start Local Proxy Server
        new Thread(() -> {
            try {
                proxyServer = new ServerSocket(LOCAL_PROXY_PORT); // Initialize proxyServer
                ui.log("✅ VPN Client running as local proxy on port " + LOCAL_PROXY_PORT);
                while (running) {
                    Socket clientSocket = proxyServer.accept();
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            } catch (IOException e) {
                ui.log("❌ Error starting proxy: " + e.getMessage());
            }
        }).start();
    }

    public void stopClient() {
        running = false;  // Stop the client logic
        ui.log("🛑 VPN Client stopped.");

        // Close the proxy server if it's running
        try {
            if (proxyServer != null && !proxyServer.isClosed()) {
                proxyServer.close();
                ui.log("✅ Proxy server stopped.");
            }
        } catch (IOException e) {
            ui.log("❌ Error stopping proxy server: " + e.getMessage());
        }

        // Close VPN connection if it's active
        try {
            if (vpnSocket != null && !vpnSocket.isClosed()) {
                vpnSocket.close();
                ui.log("✅ VPN Server connection closed.");
            }
        } catch (IOException e) {
            ui.log("❌ Error closing VPN connection: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                InputStream clientIn = clientSocket.getInputStream();
                OutputStream clientOut = clientSocket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientIn));
                PrintWriter writer = new PrintWriter(clientOut, true)
        ) {
            String requestLine = reader.readLine();
            if (requestLine == null) return;

            ui.log("📥 Incoming request: " + requestLine);

            String[] parts = requestLine.split(" ");
            if (parts.length < 3) return;

            String method = parts[0];

            if ("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) {
                forwardHttpRequestToVPNServer(requestLine, reader, clientOut);
            } else {
                ui.log("❌ Rejecting non-HTTP request: " + method);
                writer.println("HTTP/1.1 405 Method Not Allowed");
                writer.println("Content-Type: text/plain");
                writer.println("Connection: close");
                writer.println();
                writer.println("405 Method Not Allowed");
                writer.flush();
            }

        } catch (Exception e) {
            ui.log("❌ Error handling client: " + e.getMessage());
        }
    }

    private void forwardHttpRequestToVPNServer(String requestLine, BufferedReader clientReader, OutputStream clientOut) {
        try (Socket vpnSocket = new Socket()) {
            vpnSocket.connect(new InetSocketAddress(VPN_SERVER_IP, VPN_SERVER_PORT));

            OutputStream vpnOut = vpnSocket.getOutputStream();
            BufferedReader vpnIn = new BufferedReader(new InputStreamReader(vpnSocket.getInputStream()));
            PrintWriter vpnWriter = new PrintWriter(vpnOut, true);

            ui.log("🌍 Forwarding HTTP request to VPN server: " + requestLine);

            vpnWriter.println(requestLine);

            String line;
            while (!(line = clientReader.readLine()).isEmpty()) {
                vpnWriter.println(line);
            }
            vpnWriter.println();
            vpnWriter.flush();

            String responseLine;
            while ((responseLine = vpnIn.readLine()) != null) {
                clientOut.write(responseLine.getBytes());
                clientOut.write("\r\n".getBytes());
            }
            clientOut.flush();

        } catch (IOException e) {
            ui.log("❌ Error forwarding HTTP request to VPN server: " + e.getMessage());
        }
    }
}
