package com.denari.manager.controllers;

import com.denari.manager.services.BankAccountProxyService;
import com.denari.manager.security.JwtAuthenticationToken;
import com.denari.manager.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@Slf4j
public class BankAccountProxyController {

    @Autowired
    private BankAccountProxyService bankAccountProxyService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Create external bank account via Modern Treasury proxy
     * Sensitive data is sent directly to Modern Treasury, never stored on our servers
     */
    @PostMapping("/connect-bank-account")
    public ResponseEntity<?> connectBankAccount(
            @RequestBody Map<String, String> bankDetails,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            log.info("🏦 PROXY: Creating external account for user: {}", userId);

            // Validate required fields
            if (!isValidBankDetails(bankDetails)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Missing required bank account information"
                ));
            }

            // Create external account via Modern Treasury (handles sensitive data)
            String externalAccountId = bankAccountProxyService.createExternalAccountSecurely(userId, bankDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bank account connected successfully");
            response.put("externalAccountId", externalAccountId);
            response.put("nextStep", "VIRTUAL_ACCOUNT_SETUP");
            response.put("progress", 90);

            log.info("✅ PROXY: External account created successfully for user: {}", userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ PROXY: Error connecting bank account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to connect bank account: " + e.getMessage()
            ));
        }
    }

    /**
     * Validate bank details (basic validation only)
     */
    private boolean isValidBankDetails(Map<String, String> bankDetails) {
        String[] requiredFields = {"bankName", "accountType", "routingNumber", "accountNumber", "accountName"};

        for (String field : requiredFields) {
            if (bankDetails.get(field) == null || bankDetails.get(field).trim().isEmpty()) {
                log.warn("Missing required field: {}", field);
                return false;
            }
        }

        // Basic routing number validation
        String routingNumber = bankDetails.get("routingNumber");
        if (routingNumber.length() != 9 || !routingNumber.matches("\\d{9}")) {
            log.warn("Invalid routing number format");
            return false;
        }

        return true;
    }

    /**
     * Extract user ID from JWT token - FIXED
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        try {
            // ✅ FIX: Use the correct approach like in OnboardingController
            if (authentication instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
                String token = (String) jwtAuth.getCredentials();
                return jwtUtil.extractUserId(token);
            }
            throw new RuntimeException("Invalid authentication token type");
        } catch (Exception e) {
            log.error("Failed to extract user ID from authentication: {}", e.getMessage());
            throw new RuntimeException("Failed to extract user ID from authentication");
        }
    }
}
