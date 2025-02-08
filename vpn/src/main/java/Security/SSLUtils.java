package Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.security.KeyStore;

public class SSLUtils {
    private static final String KEYSTORE_PATH = "/server_keystore.jks"; // Ensure correct path in resources
    private static final String KEYSTORE_PASSWORD = "password";

    public static SSLServerSocketFactory getSSLServerSocketFactory() throws Exception {
        try (InputStream keyStoreFile = SSLUtils.class.getResourceAsStream(KEYSTORE_PATH)) {
            if (keyStoreFile == null) {
                throw new Exception("Keystore file not found: " + KEYSTORE_PATH);
            }
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreFile, KEYSTORE_PASSWORD.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            return sslContext.getServerSocketFactory();
        }
    }

    public static SSLSocketFactory getSSLSocketFactory() throws Exception {
        try (InputStream keyStoreFile = SSLUtils.class.getResourceAsStream(KEYSTORE_PATH)) {
            if (keyStoreFile == null) {
                throw new Exception("Keystore file not found: " + KEYSTORE_PATH);
            }
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreFile, KEYSTORE_PASSWORD.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            return sslContext.getSocketFactory();
        }
    }
}