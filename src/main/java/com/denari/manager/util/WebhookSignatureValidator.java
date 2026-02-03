package com.denari.manager.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
public class WebhookSignatureValidator {

    private static final String HMAC_SHA256 = "HmacSHA256";

    @Value("${moderntreasury.webhookSecret}")
    private String webhookSecret;

    public WebhookSignatureValidator(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public boolean isValidSignature(String payload, String signatureHeader) {

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            String computedSignature = sb.toString();
            log.info("Computed Signature: {}", computedSignature);

            // Compare computed signature to the header
            return computedSignature.equals(signatureHeader);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
