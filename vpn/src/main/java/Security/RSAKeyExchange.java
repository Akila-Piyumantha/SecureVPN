package Security;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import java.nio.file.*;

public class RSAKeyExchange {
    private static final String PUBLIC_KEY_FILE = "public.key";
    private static final String PRIVATE_KEY_FILE = "private.key";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSAKeyExchange() throws Exception {
        if (Files.exists(Paths.get(PUBLIC_KEY_FILE)) && Files.exists(Paths.get(PRIVATE_KEY_FILE))) {
            this.publicKey = loadPublicKey();
            this.privateKey = loadPrivateKey();
        } else {
            generateKeyPair();
        }
    }

    private void generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        this.publicKey = pair.getPublic();
        this.privateKey = pair.getPrivate();

        Files.write(Paths.get(PUBLIC_KEY_FILE), publicKey.getEncoded());
        Files.write(Paths.get(PRIVATE_KEY_FILE), privateKey.getEncoded());
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    private PublicKey loadPublicKey() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(PUBLIC_KEY_FILE));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
    }

    private PrivateKey loadPrivateKey() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(PRIVATE_KEY_FILE));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    public byte[] decryptWithPrivateKey(byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }
}
