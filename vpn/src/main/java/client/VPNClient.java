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

    public VPNClient(VPNClientGUI ui) {
        this.ui = ui;
    }


    public void startClient() {
        running = true;
        new Thread(() -> {
            try (ServerSocket proxyServer = new ServerSocket(LOCAL_PROXY_PORT)) {
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
        running = false;
        ui.log("🛑 VPN Client stopped.");
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
