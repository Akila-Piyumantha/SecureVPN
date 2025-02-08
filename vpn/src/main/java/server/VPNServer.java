package server;

import Security.SSLUtils;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;
import java.util.concurrent.*;

public class VPNServer {
    private static final int PORT = 4433;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try {
            SSLServerSocketFactory sslServerSocketFactory = SSLUtils.getSSLServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(PORT);
            System.out.println("VPN Server started on port " + PORT);

            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                threadPool.execute(new clientHandler(clientSocket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}