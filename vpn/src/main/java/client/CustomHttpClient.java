package client;

import java.net.http.HttpClient;
import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class CustomHttpClient {
    public static HttpClient createInsecureHttpClient() throws Exception {
        // Trust manager that ignores SSL certificate validation
        TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
        };

        // Initialize SSL context with our custom trust manager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCertificates, new SecureRandom());

        // Create HttpClient with the custom SSL context
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();
    }
}
