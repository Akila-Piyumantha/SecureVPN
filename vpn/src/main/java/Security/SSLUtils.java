package Security;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SSLUtils {
    private static final String SERVER_KEYSTORE = "server_keystore.jks";
    private static final String SERVER_TRUSTSTORE = "server_truststore.jks";
    private static final String CLIENT_TRUSTSTORE = "client_truststore.jks";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final String TRUSTSTORE_PASSWORD = "password";

    /**
     * Creates an SSLServerSocketFactory for the server.
     */
    public static SSLServerSocketFactory getSSLServerSocketFactory() throws Exception {
        // Load the server's keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(SERVER_KEYSTORE)) {
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
        }

        // Initialize KeyManagerFactory with the keystore
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        // Load the server's truststore (to trust client certificates)
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(SERVER_TRUSTSTORE)) {
            trustStore.load(fis, TRUSTSTORE_PASSWORD.toCharArray());
        }

        // Initialize TrustManagerFactory with the truststore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);

        // Create and initialize SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext.getServerSocketFactory();
    }

    /**
     * Creates an SSLSocketFactory for the client.
     */
    public static SSLSocketFactory getSSLSocketFactory() throws Exception {
        // Load the client's truststore (to trust the server's certificate)
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(CLIENT_TRUSTSTORE)) {
            trustStore.load(fis, TRUSTSTORE_PASSWORD.toCharArray());
        }

        // Initialize TrustManagerFactory with the truststore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);

        // Create and initialize SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }

    /**
     * Generates a keystore and truststore for the server and client.
     * This is a one-time setup step.
     */
    public static void generateKeyStores() throws Exception {
        // Generate server keystore
        KeyStore serverKeyStore = KeyStore.getInstance("JKS");
        serverKeyStore.load(null, null);

        // Generate server certificate and private key
        // (This step requires a tool like keytool or OpenSSL)
        // For simplicity, we assume the keystore and truststore are pre-generated.

        // Save the server keystore to a file
        try (FileOutputStream fos = new FileOutputStream(SERVER_KEYSTORE)) {
            serverKeyStore.store(fos, KEYSTORE_PASSWORD.toCharArray());
        }

        // Generate server truststore
        KeyStore serverTrustStore = KeyStore.getInstance("JKS");
        serverTrustStore.load(null, null);

        // Add the server's certificate to the truststore
        // (This step requires the server's certificate)
        // For simplicity, we assume the truststore is pre-generated.

        // Save the server truststore to a file
        try (FileOutputStream fos = new FileOutputStream(SERVER_TRUSTSTORE)) {
            serverTrustStore.store(fos, TRUSTSTORE_PASSWORD.toCharArray());
        }

        // Generate client truststore
        KeyStore clientTrustStore = KeyStore.getInstance("JKS");
        clientTrustStore.load(null, null);

        // Add the server's certificate to the client truststore
        // (This step requires the server's certificate)
        // For simplicity, we assume the truststore is pre-generated.

        // Save the client truststore to a file
        try (FileOutputStream fos = new FileOutputStream(CLIENT_TRUSTSTORE)) {
            clientTrustStore.store(fos, TRUSTSTORE_PASSWORD.toCharArray());
        }
    }
}