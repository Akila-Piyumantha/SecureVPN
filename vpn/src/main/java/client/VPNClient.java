package client;

import Security.SSLUtils;
import ui.VPNClientGUI;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VPNClient {
    private static final String VPN_SERVER_IP = "localhost"; // Or remote VPN server IP
    private static final int VPN_SERVER_PORT = 443;            // VPN server's SSL port
    private static final int LOCAL_PROXY_PORT = 3128;          // Local proxy port for clients

    private final VPNClientGUI ui;
    private volatile boolean running = false;
    private ServerSocket proxyServer;
    private SSLSocket vpnSocket;
    private BufferedReader vpnIn;
    private PrintWriter vpnOut;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public VPNClient(VPNClientGUI ui) {
        this.ui = ui;
        // Shutdown hook for graceful exit
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopClient));
    }

    public void startClient() {
        running = true;
        // Start VPN connection in its own thread
        threadPool.execute(this::connectToVPNServer);
        // Start local proxy listener
        threadPool.execute(this::startProxy);
    }

    /**
     * Connects to the VPN server over SSL.
     */
    private synchronized void connectToVPNServer() {
        try {
            vpnSocket = (SSLSocket) SSLUtils.getSSLSocketFactory().createSocket(VPN_SERVER_IP, VPN_SERVER_PORT);
            // Initialize readers and writers for this VPN connection.
            vpnIn = new BufferedReader(new InputStreamReader(vpnSocket.getInputStream()));
            vpnOut = new PrintWriter(vpnSocket.getOutputStream(), true);
            ui.log("🌐 Connected to VPN Server at " + VPN_SERVER_IP + ":" + VPN_SERVER_PORT + " via SSL");
        } catch (SSLHandshakeException e) {
            ui.log("❌ SSL Handshake failed: " + e.getMessage());
        } catch (Exception e) {
            ui.log("❌ Error connecting to VPN server: " + e.getMessage());
        }
    }

    /**
     * Starts the local proxy server to accept client connections.
     */
    private void startProxy() {
        try (ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(LOCAL_PROXY_PORT));
            proxyServer = server;
            ui.log("✅ VPN Client running as local proxy on port " + LOCAL_PROXY_PORT);
            while (running) {
                Socket clientSocket = proxyServer.accept();
                threadPool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            ui.log("❌ Error starting proxy: " + e.getMessage());
        }
    }

    /**
     * Handles an individual client connection.
     */
    private void handleClient(Socket clientSocket) {
        try (InputStream clientIn = clientSocket.getInputStream();
             OutputStream clientOut = clientSocket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(clientIn));
             PrintWriter writer = new PrintWriter(clientOut, true)) {

            // Read the initial request line.
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                sendErrorResponse(writer, 400, "Bad Request");
                return;
            }
            ui.log("📥 Incoming request: " + requestLine);

            // We only support GET and POST in this example.
            String[] parts = requestLine.split(" ");
            if (parts.length < 3 ||
                    !(parts[0].equalsIgnoreCase("GET") || parts[0].equalsIgnoreCase("POST"))) {
                ui.log("❌ Rejecting non-HTTP/GET/POST request: " + requestLine);
                sendErrorResponse(writer, 405, "Method Not Allowed");
                return;
            }

            forwardToVPN(requestLine, reader, clientOut);
        } catch (IOException e) {
            ui.log("❌ Error handling client: " + e.getMessage());
        }
    }

    /**
     * Forwards the HTTP request to the VPN server and then relays the full response
     * (headers and body) back to the local client.
     */
    private synchronized void forwardToVPN(String request, BufferedReader clientReader, OutputStream clientOut) {
        try {
            // Ensure VPN connection is live.
            if (vpnSocket == null || vpnSocket.isClosed()) {
                ui.log("🔄 Reconnecting to VPN server...");
                connectToVPNServer();
            }

            // Send the request line.
            vpnOut.println(request);
            // Forward remaining headers from client.
            String headerLine;
            while ((headerLine = clientReader.readLine()) != null && !headerLine.isEmpty()) {
                vpnOut.println(headerLine);
            }
            // End of request headers.
            vpnOut.println();
            vpnOut.flush();
            ui.log("🌍 Forwarded request to VPN server: " + request);

            // Now, read the response from the VPN server.
            // We first use a BufferedReader to read the response headers.
            InputStream vpnRaw = vpnSocket.getInputStream();
            BufferedReader headerReader = new BufferedReader(new InputStreamReader(vpnRaw));
            StringBuilder responseHeaders = new StringBuilder();

            // Read the status line.
            String line = headerReader.readLine();
            if (line == null) {
                ui.log("❌ No response from VPN server.");
                return;
            }
            responseHeaders.append(line).append("\r\n");

            // Parse headers.
            int contentLength = -1;
            boolean chunked = false;
            while ((line = headerReader.readLine()) != null && !line.isEmpty()) {
                responseHeaders.append(line).append("\r\n");
                String lower = line.toLowerCase();
                if (lower.startsWith("content-length:")) {
                    try {
                        contentLength = Integer.parseInt(line.substring("content-length:".length()).trim());
                    } catch (NumberFormatException e) {
                        ui.log("❌ Error parsing Content-Length: " + e.getMessage());
                    }
                } else if (lower.startsWith("transfer-encoding:") && lower.contains("chunked")) {
                    chunked = true;
                }
            }
            // End of headers.
            responseHeaders.append("\r\n");
            // Forward the response headers to the client.
            clientOut.write(responseHeaders.toString().getBytes());
            clientOut.flush();

            // Now, forward the response body.
            if (chunked) {
                // Process chunked encoding.
                String chunkSizeLine;
                while ((chunkSizeLine = headerReader.readLine()) != null) {
                    // Forward chunk size.
                    clientOut.write((chunkSizeLine + "\r\n").getBytes());
                    clientOut.flush();
                    int chunkSize;
                    try {
                        chunkSize = Integer.parseInt(chunkSizeLine.trim(), 16);
                    } catch (NumberFormatException e) {
                        ui.log("❌ Error parsing chunk size: " + e.getMessage());
                        break;
                    }
                    if (chunkSize == 0) {
                        // Last chunk; forward trailing headers.
                        String trailingLine;
                        while ((trailingLine = headerReader.readLine()) != null) {
                            clientOut.write((trailingLine + "\r\n").getBytes());
                            if (trailingLine.isEmpty()) break;
                        }
                        break;
                    }
                    // Read and forward this chunk.
                    byte[] chunkData = new byte[chunkSize];
                    int totalRead = 0;
                    while (totalRead < chunkSize) {
                        int bytesRead = vpnRaw.read(chunkData, totalRead, chunkSize - totalRead);
                        if (bytesRead == -1) break;
                        totalRead += bytesRead;
                    }
                    clientOut.write(chunkData, 0, totalRead);
                    // Read and forward the trailing CRLF after chunk data.
                    String crlf = headerReader.readLine();
                    if (crlf != null) {
                        clientOut.write((crlf + "\r\n").getBytes());
                    }
                    clientOut.flush();
                }
            } else if (contentLength >= 0) {
                // Read exactly Content-Length bytes.
                byte[] buffer = new byte[4096];
                int remaining = contentLength;
                int bytesRead;
                while (remaining > 0 && (bytesRead = vpnRaw.read(buffer, 0, Math.min(buffer.length, remaining))) != -1) {
                    clientOut.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
                clientOut.flush();
            } else {
                // If no Content-Length and not chunked, read until the stream is closed.
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = vpnRaw.read(buffer)) != -1) {
                    clientOut.write(buffer, 0, bytesRead);
                }
                clientOut.flush();
            }
        } catch (IOException e) {
            ui.log("❌ Error forwarding HTTP request to VPN server: " + e.getMessage());
        }
    }

    /**
     * Sends an HTTP error response to the client.
     */
    private void sendErrorResponse(PrintWriter writer, int statusCode, String message) {
        writer.printf("HTTP/1.1 %d %s\r\nContent-Type: text/plain\r\nConnection: close\r\n\r\n%s\r\n",
                statusCode, message, message);
        writer.flush();
    }

    public void stopClient() {
        running = false;
        threadPool.shutdownNow();
        try {
            if (vpnOut != null) vpnOut.close();
            if (vpnIn != null) vpnIn.close();
            if (vpnSocket != null) vpnSocket.close();
            if (proxyServer != null && !proxyServer.isClosed()) proxyServer.close();
            ui.log("🛑 VPN Client stopped.");
        } catch (IOException e) {
            ui.log("❌ Error closing VPN connection: " + e.getMessage());
        }
    }
}
