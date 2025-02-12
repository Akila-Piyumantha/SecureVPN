// VPNClient.java (Client Package)
package client;

import Security.SSLUtils;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

public class VPNClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 4433;
    private SSLSocket socket;
    private final ui.VPNClientGUI ui;

    public static void main(String[] args) {
        new VPNClient();
    }

    public VPNClient() {
        ui = new ui.VPNClientGUI(this);
    }

    public void connectToServer() {
        try {
            SSLSocketFactory factory = SSLUtils.getSSLSocketFactory();
            socket = (SSLSocket) factory.createSocket(SERVER_ADDRESS, SERVER_PORT);
            ui.updateStatus("Connected to VPN Server");
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
