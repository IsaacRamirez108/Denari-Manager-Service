package com.denari.manager.controllers;

import com.denari.manager.enums.ProcessingStatus;
import com.denari.manager.models.dto.WebhookPayload;
import com.denari.manager.repositories.WebhookEventRepository;
import com.denari.manager.services.Webhook.WebhookRoutingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    // ✅ CHANGE: Replace ModernTreasuryWebhookHandler with WebhookRoutingService
    private final WebhookRoutingService webhookRoutingService; // ✅ NEW
    private final WebhookEventRepository webhookEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${moderntreasury.webhookSecret}")
    private String webhookSecret;

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * ✅ UPDATED: Main webhook endpoint - now uses routing system
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature) {

        try {
            log.info("📨 Received webhook payload (size: {} bytes)", payload.length());

            // 1. Validate webhook signature for security
            if (!isValidSignature(payload, signature)) {
                log.warn("⚠️ Invalid webhook signature received");
                return ResponseEntity.status(401)
                        .body("{\"success\": false, \"message\": \"Invalid signature\"}");
            }

            // 2. Parse the webhook payload
            WebhookPayload dto = parseWebhookPayload(payload);

            // 3. Check for duplicate processing (idempotency)
            String eventId = dto.getResourceId();
            if (eventId != null && webhookEventRepository.isEventAlreadyProcessed(eventId)) {
                log.info("⚠️ Webhook event {} already processed, skipping", eventId);
                return ResponseEntity.ok("{\"success\": true, \"message\": \"Event already processed\"}");
            }

            // 4. ✅ NEW: Route to appropriate specialized handler (async)
            webhookRoutingService.routeWebhook(dto);

            // 5. Return 200 OK immediately to Modern Treasury
            log.info("✅ Webhook received and routed successfully: {}", dto.getEvent());
            return ResponseEntity.ok("{\"success\": true, \"message\": \"Webhook processed\"}");

        } catch (JsonProcessingException e) {
            log.error("❌ Failed to parse webhook payload: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("{\"success\": false, \"message\": \"Invalid JSON payload\"}");

        } catch (Exception e) {
            log.error("❌ Unexpected error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body("{\"success\": false, \"message\": \"Internal server error\"}");
        }
    }

    /**
     * ✅ NEW: Parse webhook payload with validation
     */
    private WebhookPayload parseWebhookPayload(String payload) throws JsonProcessingException {
        WebhookPayload dto = objectMapper.readValue(payload, WebhookPayload.class);

        // Validate required fields
        if (dto.getData() == null || !dto.getData().has("id")) {
            throw new IllegalArgumentException("Invalid webhook payload - missing data or ID");
        }

        if (dto.getEvent() == null || dto.getEvent().isEmpty()) {
            throw new IllegalArgumentException("Invalid webhook payload - missing event type");
        }

        log.info("📋 Parsed webhook: Event={}, ResourceID={}", dto.getEvent(), dto.getResourceId());
        return dto;
    }

    /**
     * ✅ UPDATED: Verify webhook signature using HMAC SHA256
     * PRODUCTION SECURITY: Always validate signatures in production!
     */
    private boolean isValidSignature(String payload, String signatureHeader) {
        // ✅ FOR TESTING: Allow empty webhook secret to skip validation
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            log.warn("⚠️ Webhook signature validation disabled (empty secret)");
            return true;
        }

        if (signatureHeader == null) {
            log.warn("⚠️ Missing signature header");
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            String computedSignature = sb.toString();

            // Compare signatures (constant-time comparison for security)
            boolean isValid = constantTimeEquals(computedSignature, signatureHeader);

            if (!isValid) {
                log.warn("⚠️ Signature mismatch - Expected: {}, Got: {}",
                        computedSignature.substring(0, 8) + "...",
                        signatureHeader.substring(0, Math.min(8, signatureHeader.length())) + "...");
            }

            return isValid;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("❌ Error computing webhook signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ✅ NEW: Constant-time string comparison to prevent timing attacks
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * ✅ NEW: Health check endpoint for webhook system
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        try {
            // Check recent webhook processing health
            long recentEvents = webhookEventRepository.count();
            // ✅ FIXED: Use String instead of enum
            long processedEvents = webhookEventRepository.countByProcessingStatus(ProcessingStatus.valueOf("COMPLETED"));

            return ResponseEntity.ok(String.format(
                    "{\"status\": \"healthy\", \"total_events\": %d, \"processed_events\": %d, \"success_rate\": \"%.2f%%\"}",
                    recentEvents, processedEvents,
                    recentEvents > 0 ? (processedEvents * 100.0 / recentEvents) : 0.0));

        } catch (Exception e) {
            log.error("❌ Webhook health check failed: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body("{\"status\": \"unhealthy\", \"error\": \"Database connection failed\"}");
        }
    }

    /**
     * ✅ NEW: Admin endpoint to get webhook processing statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getWebhookStats() {
        try {
            var stats = webhookRoutingService.getRoutingStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Error getting webhook stats: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body("{\"error\": \"Failed to get webhook statistics\"}");
        }
    }
}
