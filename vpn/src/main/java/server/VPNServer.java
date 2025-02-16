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
                new Thread(new VPNHandler(clientSocket, ui)).start();
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

    public VPNHandler(Socket socket, VPNServerGUI ui) {
        this.clientSocket = socket;
        this.ui = ui;
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
            if (requestLine == null) {
                return;
            }

            ui.log("📥 Incoming Request: " + requestLine);

            String host = null;
            String line;
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
            ui.log("Error in VPNHandler: " + e.getMessage());
        }
    }

    private void forwardHttpRequest(String host, String requestLine, BufferedReader clientReader, PrintWriter clientWriter) {
        try (Socket serverSocket = new Socket(host, 80)) {
            ui.log("🌍 Forwarding HTTP request to " + host);

            OutputStream serverOut = serverSocket.getOutputStream();
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            PrintWriter serverWriter = new PrintWriter(serverOut, true);
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
            ui.log("❌ Error forwarding request: " + e.getMessage());
        }
    }
}
