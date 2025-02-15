// VPNClient.java (Client Package)
package client;

import Security.SSLUtils;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

public class VPNClient {
    private static final String SERVER_ADDRESS = "127.0.0.1"; // Default server address
    private static final int SERVER_PORT = 4433; // Default port
    private SSLSocket socket;
    private final ui.VPNClientGUI ui;

    public static void main(String[] args) {
        new VPNClient();
    }

    public VPNClient() {
        ui = new ui.VPNClientGUI(this);
    }

    // Overloaded connectToServer() method to maintain compatibility with GUI
    public void connectToServer() {
        connectToServer(SERVER_ADDRESS, SERVER_PORT);
    }

    // New method to allow dynamic connection parameters
    public void connectToServer(String serverAddress, int serverPort) {
        try {
            SSLSocketFactory factory = SSLUtils.getSSLSocketFactory();
            socket = (SSLSocket) factory.createSocket(serverAddress, serverPort);
            ui.updateStatus("Connected to VPN Server at " + serverAddress + ":" + serverPort);
            ui.setConnectionState(true);
        } catch (Exception e) {
            ui.updateStatus("Connection failed: " + e.getMessage());
        }
    }

    public void disconnectFromServer() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                ui.updateStatus("Disconnected from VPN Server");
            }
        } catch (IOException e) {
            ui.updateStatus("Error disconnecting: " + e.getMessage());
        }
        ui.setConnectionState(false);
    }
}
