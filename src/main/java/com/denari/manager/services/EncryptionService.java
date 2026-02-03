package com.denari.manager.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionService {

    @Value("${app.encryption.key:your-32-character-secret-key-here}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    /**
     * Encrypt SSN for secure storage
     */
    public String encryptSSN(String plainSSN) {
        try {
            if (plainSSN == null || plainSSN.trim().isEmpty()) {
                throw new IllegalArgumentException("SSN cannot be null or empty");
            }

            // Clean SSN (remove dashes, spaces)
            String cleanSSN = plainSSN.replaceAll("[^0-9]", "");

            if (cleanSSN.length() != 9) {
                throw new IllegalArgumentException("SSN must be 9 digits");
            }

            SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] encryptedData = cipher.doFinal(cleanSSN.getBytes());

            // Combine IV + encrypted data
            byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);

            String encrypted = Base64.getEncoder().encodeToString(encryptedWithIv);
            log.info("SSN encrypted successfully");
            return encrypted;

        } catch (Exception e) {
            log.error("Error encrypting SSN: {}", e.getMessage());
            throw new RuntimeException("Failed to encrypt SSN");
        }
    }

    /**
     * Create masked SSN for UI display (XXX-XX-1234)
     */
    public String createMaskedSSN(String plainSSN) {
        try {
            if (plainSSN == null || plainSSN.trim().isEmpty()) {
                return "XXX-XX-XXXX";
            }

            // Clean SSN (remove dashes, spaces)
            String cleanSSN = plainSSN.replaceAll("[^0-9]", "");

            if (cleanSSN.length() != 9) {
                return "XXX-XX-XXXX";
            }

            // Return masked format: XXX-XX-1234
            return "XXX-XX-" + cleanSSN.substring(5);

        } catch (Exception e) {
            log.warn("Could not create masked SSN: {}", e.getMessage());
            return "XXX-XX-XXXX";
        }
    }

    /**
     * Validate SSN format
     */
    public boolean isValidSSN(String ssn) {
        if (ssn == null) return false;
        String cleaned = ssn.replaceAll("[^0-9]", "");
        return cleaned.length() == 9 && !cleaned.equals("000000000");
    }
}
