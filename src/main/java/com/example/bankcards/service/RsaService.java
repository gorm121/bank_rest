package com.example.bankcards.service;



import com.example.bankcards.exception.InvalidDataException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@Slf4j
public class RsaService {

    @Value("${app.rsa.private-key:}")
    private String privateKeyBase64;

    @Value("${app.rsa.public-key:}")
    private String publicKeyBase64;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        if (StringUtils.hasText(privateKeyBase64) && StringUtils.hasText(publicKeyBase64)) {
            loadKeysFromConfig();
        } else {
            log.error("Private key or public key could not be loaded");
            throw new RuntimeException("Keys for RSA are not initialized");
        }
    }


    private void loadKeysFromConfig() throws Exception {
        String publicClean = publicKeyBase64
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        String privateClean = privateKeyBase64
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] publicBytes = Base64.getDecoder().decode(publicClean);
        byte[] privateBytes = Base64.getDecoder().decode(privateClean);

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

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        }catch(Exception e){
            throw new InvalidDataException("Error while trying to decrypt RSA encrypted data.");
        }

    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

}
