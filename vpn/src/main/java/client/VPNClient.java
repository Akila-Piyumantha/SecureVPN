package client;

import java.io.*;
import java.net.*;

public class VPNClient {
    private static final String SERVER_IP = "127.0.0.1"; // VPN Server IP
    private static final int SERVER_PORT = 4433;

    private final ui.VPNClientGUI ui;

    public static void main(String[] args) {
        new VPNClient();
    }

    public VPNClient() {
        ui = new ui.VPNClientGUI(this);
        connectToServer();
    }

    public void connectToServer() {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            ui.updateStatus("Connected to VPN Server");
            ui.setConnectionState(true);

            // Start forwarding packets in a new thread
            new Thread(() -> forwardTraffic(socket)).start();
        } catch (IOException e) {
            ui.updateStatus("Connection failed: " + e.getMessage());
        }
    }

    public void disconnectFromServer() {
        ui.setConnectionState(false);
        ui.updateStatus("Disconnected from VPN Server");
    }

    private static void forwardTraffic(Socket socket) {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            byte[] buffer = new byte[4096];
            while (true) {
                int bytesRead = in.read(buffer);
                if (bytesRead == -1) break; // End of stream, stop forwarding

                // Forward the data to the output stream (VPN Tunnel)
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

