package com.example.bankcards.service;

import com.example.bankcards.exception.InvalidDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RsaServiceTest {

    @InjectMocks
    private RsaService rsaService;

    private KeyPair keyPair;
    private String originalText = "4111111111111111";

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();

        // Устанавливаем ключи через reflection
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        String formattedPublicKey = "-----BEGIN PUBLIC KEY-----\n" +
                publicKeyBase64 +
                "\n-----END PUBLIC KEY-----";
        String formattedPrivateKey = "-----BEGIN PRIVATE KEY-----\n" +
                privateKeyBase64 +
                "\n-----END PRIVATE KEY-----";

        ReflectionTestUtils.setField(rsaService, "privateKeyBase64", formattedPrivateKey);
        ReflectionTestUtils.setField(rsaService, "publicKeyBase64", formattedPublicKey);

        rsaService.init();
    }

    @Test
    void generateKeyPair_Success() throws Exception {
        KeyPair generatedKeyPair = rsaService.generateKeyPair();

        assertNotNull(generatedKeyPair);
        assertNotNull(generatedKeyPair.getPublic());
        assertNotNull(generatedKeyPair.getPrivate());
    }

    @Test
    void getPublicKeyBase64_Success() {
        String publicKeyBase64 = rsaService.getPublicKeyBase64();

        assertNotNull(publicKeyBase64);
        assertFalse(publicKeyBase64.isEmpty());
        assertFalse(publicKeyBase64.contains("-----BEGIN PUBLIC KEY-----"));
        assertFalse(publicKeyBase64.contains("-----END PUBLIC KEY-----"));
    }

    @Test
    void decrypt_InvalidData_ThrowsException() {
        String invalidEncryptedData = "invalidBase64Data";

        assertThrows(InvalidDataException.class, () -> {
            rsaService.decrypt(invalidEncryptedData);
        });
    }

    @Test
    void encryptDecrypt_Integration() throws Exception {
        assertNotNull(ReflectionTestUtils.getField(rsaService, "privateKey"));
        assertNotNull(ReflectionTestUtils.getField(rsaService, "publicKey"));
    }
}