package com.example.bankcards.service;



import com.example.bankcards.exception.InvalidDataException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class RsaService {

    @Value("${app.rsa.private-key:}")
    private String privateKeyBase64;

    @Value("${app.rsa.public-key:}")
    private String publicKeyBase64;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        if (privateKeyBase64 != null && !privateKeyBase64.isEmpty() &&
                publicKeyBase64 != null && !publicKeyBase64.isEmpty()) {
            loadKeysFromConfig();
        } else {
            generateAndSaveKeys();
        }
    }

    private void generateAndSaveKeys() throws Exception {
        KeyPair keyPair = generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();

        System.out.println("Generated new RSA keys. Add to application.properties:");
        System.out.println("app.rsa.public-key=" + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        System.out.println("app.rsa.private-key=" + Base64.getEncoder().encodeToString(privateKey.getEncoded()));
    }

    private void loadKeysFromConfig() throws Exception {
        byte[] publicBytes = Base64.getDecoder().decode(publicKeyBase64);
        byte[] privateBytes = Base64.getDecoder().decode(privateKeyBase64);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicBytes);
        this.publicKey = keyFactory.generatePublic(publicKeySpec);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateBytes);
        this.privateKey = keyFactory.generatePrivate(privateKeySpec);
    }

    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public String decrypt(String encryptedBase64) {
        try{
            byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        }catch(Exception e){
            throw new InvalidDataException("Error while trying to decrypt RSA encrypted data.");
        }

    }


    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

}
