package client;
import Security.SSLUtils;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
public class ClientSSLUtils {
    private static final String TRUSTSTORE_PATH = "src/main/java/client/client_truststore.jks"; // Absolute path
    private static final String TRUSTSTORE_PASSWORD = "password";

    public static SSLSocketFactory getSSLSocketFactory() throws Exception {
        try (InputStream trustStoreFile = new FileInputStream(TRUSTSTORE_PATH)) {
            System.out.println("path = "+TRUSTSTORE_PATH);
            if (trustStoreFile == null) {
                throw new Exception("Truststore file not found: " + TRUSTSTORE_PATH);
            }

            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(trustStoreFile, TRUSTSTORE_PASSWORD.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            return sslContext.getSocketFactory();
        }
    }
}

