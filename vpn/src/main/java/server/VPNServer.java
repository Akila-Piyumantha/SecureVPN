package server;
import ui.VPNServerGUI;
import java.io.*;
import java.net.*;

public class VPNServer {
    private static final int PORT = 443;
    private ServerSocket serverSocket;
    private boolean running = false;
    private VPNServerGUI ui;

    public VPNServer(VPNServerGUI ui) {
        this.ui = ui;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            ui.log("✅ VPN Server started on port " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                String userAddress = clientSocket.getInetAddress().getHostAddress();
                ui.addUser(userAddress);
                new Thread(new VPNHandler(clientSocket, ui, userAddress)).start();
            }
        } catch (IOException e) {
            ui.log("❌ Server Error: " + e.getMessage());
        }
    }

    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            ui.log("🛑 VPN Server stopped.");
        } catch (IOException e) {
            ui.log("❌ Error stopping server: " + e.getMessage());
        }
    }
}

class VPNHandler implements Runnable {
    private final Socket clientSocket;
    private final VPNServerGUI ui;
    private final String userAddress;

    public VPNHandler(Socket socket, VPNServerGUI ui, String userAddress) {
        this.clientSocket = socket;
        this.ui = ui;
        this.userAddress = userAddress;
    }

    @Override
    public void run() {
        try (
                InputStream clientIn = clientSocket.getInputStream();
                OutputStream clientOut = clientSocket.getOutputStream()
        ) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientIn));
            PrintWriter writer = new PrintWriter(clientOut, true);

            String requestLine = reader.readLine();
            if (requestLine == null) return;

            ui.log("📥 Incoming Request from " + userAddress + ": " + requestLine);

            String line, host = null;
            while (!(line = reader.readLine()).isEmpty()) {
                if (line.toLowerCase().startsWith("host: ")) {
                    host = line.substring(6).trim();
                }
            }

            if (host == null) {
                ui.log("❌ No Host Header Found!");
                return;
            }

            forwardHttpRequest(host, requestLine, reader, writer);
        } catch (IOException e) {
            ui.log("Error handling user " + userAddress + ": " + e.getMessage());
        } finally {
            ui.removeUser(userAddress);
        }
    }

    private void forwardHttpRequest(String host, String requestLine, BufferedReader clientReader, PrintWriter clientWriter) {
        try (Socket serverSocket = new Socket(host, 80)) {
            ui.log("🌍 Forwarding request from " + userAddress + " to " + host);

            PrintWriter serverWriter = new PrintWriter(serverSocket.getOutputStream(), true);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            serverWriter.println(requestLine);
            serverWriter.println("Host: " + host);
            serverWriter.println();
            serverWriter.flush();

            String responseLine;
            while ((responseLine = serverIn.readLine()) != null) {
                clientWriter.println(responseLine);
            }
            clientWriter.flush();
        } catch (IOException e) {
            ui.log("❌ Error forwarding request for user " + userAddress + ": " + e.getMessage());
        }
    }
}
