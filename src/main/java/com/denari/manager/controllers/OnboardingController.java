package com.denari.manager.controllers;

import com.denari.manager.models.dto.OnboardingSummaryResponse;
import com.denari.manager.models.entity.ExternalAccount.Counterparty;
import com.denari.manager.models.entity.User.OnboardingStatus;
import com.denari.manager.models.entity.User.User;
import com.denari.manager.models.entity.VirtualAccount.VirtualAccount;
import com.denari.manager.repositories.CounterpartyRepository;
import com.denari.manager.repositories.UserRepository;
import com.denari.manager.repositories.VirtualAccountRepository;
import com.denari.manager.security.JwtAuthenticationToken;
import com.denari.manager.security.JwtUtil;
import com.denari.manager.services.OnboardingService;
import com.denari.manager.services.ModernTreasuryService;
import com.denari.manager.services.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/onboarding")
@Slf4j
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private ModernTreasuryService modernTreasuryService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VirtualAccountRepository virtualAccountRepository;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    /**
     * Update personal information
     */
    @PostMapping("/personal-info")
    public ResponseEntity<?> updatePersonalInfo(@RequestBody Map<String, String> personalInfo,
                                                Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            log.info("Updating personal info for user: {}", userId);

            OnboardingStatus.OnboardingStep nextStep = onboardingService.updatePersonalInfo(userId, personalInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Personal information updated successfully");
            response.put("nextStep", nextStep.toString());
            response.put("progress", getProgressPercentage(nextStep));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating personal info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Update address information
     */
    @PostMapping("/address")
    public ResponseEntity<?> updateAddress(@RequestBody Map<String, String> addressInfo,
                                           Authentication authentication) {
        Long userId = null;
        try {
            userId = getUserIdFromAuth(authentication);
            log.info("🏠 ADDRESS: Starting update for user: {}", userId);
            log.info("🏠 ADDRESS: Received data: {}", addressInfo);
            log.info("🏠 ADDRESS: Data size: {} fields", addressInfo.size());

            // Log each field individually
            addressInfo.forEach((key, value) ->
                    log.info("🏠 ADDRESS: Field '{}' = '{}'", key, value));

            log.info("🏠 ADDRESS: Calling onboardingService.updateAddress...");
            OnboardingStatus.OnboardingStep nextStep = onboardingService.updateAddress(userId, addressInfo);
            log.info("🏠 ADDRESS: Service call completed, next step: {}", nextStep);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Address updated successfully");
            response.put("nextStep", nextStep.toString());
            response.put("progress", getProgressPercentage(nextStep));

            log.info("✅ ADDRESS: Success for user: {} -> Next step: {}", userId, nextStep);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ ADDRESS: Error updating address for user {}: {}", userId, e.getMessage(), e);
            log.error("❌ ADDRESS: Exception type: {}", e.getClass().getSimpleName());
            log.error("❌ ADDRESS: Stack trace first 3 lines:");
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (int i = 0; i < Math.min(3, stackTrace.length); i++) {
                log.error("❌ ADDRESS: {}", stackTrace[i]);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }


    /**
     * Update rental data
     */
    @PostMapping("/rental-data")
    public ResponseEntity<?> updateRentalData(@RequestBody Map<String, String> rentalInfo,
                                              Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            log.info("Updating rental data for user: {}", userId);

            OnboardingStatus.OnboardingStep nextStep = onboardingService.updateRentalData(userId, rentalInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rental information updated successfully");
            response.put("nextStep", nextStep.toString());
            response.put("progress", getProgressPercentage(nextStep));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating rental data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Update property manager information
     */
    @PostMapping("/property-manager")
    public ResponseEntity<?> updatePropertyManager(@RequestBody Map<String, String> managerInfo,
                                                   Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            log.info("🏢 PROPERTY-MANAGER: Starting update for user: {}", userId);
            log.info("🏢 PROPERTY-MANAGER: Received data: {}", managerInfo);
            log.info("🏢 PROPERTY-MANAGER: Data size: {} fields", managerInfo.size());

            // Log each field individually
            managerInfo.forEach((key, value) ->
                    log.info("🏢 PROPERTY-MANAGER: Field '{}' = '{}'", key, value));

            log.info("🏢 PROPERTY-MANAGER: Calling onboardingService.updatePropertyManager...");
            OnboardingStatus.OnboardingStep nextStep = onboardingService.updatePropertyManager(userId, managerInfo);
            log.info("🏢 PROPERTY-MANAGER: Service call completed, next step: {}", nextStep);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Property manager information updated successfully");
            response.put("nextStep", nextStep.toString());
            response.put("progress", getProgressPercentage(nextStep));

            log.info("✅ PROPERTY-MANAGER: Success for user: {} -> Next step: {}", userId, nextStep);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ PROPERTY-MANAGER: Error updating for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }


    /**
     * Update identity verification
     */
    @PostMapping("/identity-verification")
    public ResponseEntity<?> updateIdentityVerification(@RequestBody Map<String, String> identityInfo,
                                                        Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            log.info("Updating identity verification for user: {}", userId);

            OnboardingStatus.OnboardingStep nextStep = onboardingService.updateIdentityVerification(userId, identityInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Identity verification updated successfully");
            response.put("nextStep", nextStep.toString());
            response.put("progress", getProgressPercentage(nextStep));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating identity verification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     *  Update payment schedule with proper calculation logic
     */
    @PostMapping("/payment-schedule")
    public ResponseEntity<?> updatePaymentSchedule(@RequestBody Map<String, String> scheduleInfo,
                                                   Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            log.info("💰 Updating payment schedule for user: {}", userId);
            log.info("💰 Schedule info received: {}", scheduleInfo);

            OnboardingStatus.OnboardingStep nextStep = onboardingService.updatePaymentSchedule(userId, scheduleInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment schedule updated successfully");
            response.put("nextStep", nextStep.toString());
            response.put("progress", getProgressPercentage(nextStep));

            log.info("✅ Payment schedule updated successfully for user: {}", userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error updating payment schedule for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

//    /**
//     * Get bank connection token for Modern Treasury - PRODUCTION READY
//     */
//    @GetMapping("/bank-connection-token")
//    public ResponseEntity<?> getBankConnectionToken(Authentication authentication) {
//        try {
//            Long userId = getUserIdFromAuth(authentication);
//            log.info("Getting bank connection token for user: {}", userId);
//
//            // ✅ PRODUCTION: Check if user has required data
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            // Validate user has completed identity verification
//            if (user.getUserIdentity() == null ||
//                    user.getUserIdentity().getSsnEncrypted() == null ||
//                    user.getUserIdentity().getDateOfBirth() == null) {
//                return ResponseEntity.badRequest().body(Map.of(
//                        "success", false,
//                        "message", "Please complete identity verification first",
//                        "requiredStep", "IDENTITY_VERIFICATION"
//                ));
//            }
//
//            // ✅ PRODUCTION: Ensure counterparty exists (create if missing)
//            try {
//                Optional<Counterparty> existingCounterparty = counterpartyRepository.findByUser(user);
//                if (existingCounterparty.isEmpty()) {
//                    log.info("No counterparty found for user {}, creating one now", userId);
//                    String counterpartyId = modernTreasuryService.createCounterparty(user);
//                    log.info("Created counterparty: {}", counterpartyId);
//                }
//            } catch (Exception e) {
//                log.error("Failed to ensure counterparty exists for user {}: {}", userId, e.getMessage());
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                        "success", false,
//                        "message", "Failed to prepare user account. Please try again."
//                ));
//            }
//
//            // Generate Modern Treasury account collection flow
//            String clientToken = modernTreasuryService.createAccountCollectionFlow(userId);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("clientToken", clientToken);
//            response.put("message", "Bank connection token generated successfully");
//            response.put("nextStep", "VIRTUAL_ACCOUNT_SETUP");
//            response.put("progress", getProgressPercentage(OnboardingStatus.OnboardingStep.BANK_CONNECTION));
//
//            log.info("Bank connection token generated successfully for user: {}", userId);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("Error getting bank connection token: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of(
//                            "success", false,
//                            "message", "Failed to generate bank connection token: " + e.getMessage()
//                    ));
//        }
//    }

    /**
     * Complete virtual account setup
     */
    @PostMapping("/virtual-account-setup")
    public ResponseEntity<?> completeVirtualAccountSetup(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            log.info("Completing virtual account setup for user: {}", userId);

            OnboardingStatus.OnboardingStep nextStep = onboardingService.completeVirtualAccountSetup(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Onboarding completed successfully! Welcome to Denari!");
            response.put("nextStep", nextStep.toString());
            response.put("progress", 100);
            response.put("onboardingCompleted", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error completing virtual account setup: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get virtual account details for current user
     */
    @GetMapping("/virtual-account")
    public ResponseEntity<?> getVirtualAccount(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            log.info("Getting virtual account for user: {}", userId);

            // Get user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get virtual account
            Optional<VirtualAccount> virtualAccountOpt = virtualAccountRepository.findByUserId(userId);

            if (virtualAccountOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Virtual account not found. Please complete previous steps first."
                ));
            }

            VirtualAccount virtualAccount = virtualAccountOpt.get();

            // Build response with virtual account details
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("virtualAccount", buildVirtualAccountResponse(virtualAccount));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting virtual account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to get virtual account"));
        }
    }

    /**
     * Build virtual account response object
     */
    private Map<String, Object> buildVirtualAccountResponse(VirtualAccount virtualAccount) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", virtualAccount.getMtId());
        response.put("name", virtualAccount.getName());
        response.put("active", virtualAccount.getActive());
        response.put("description", virtualAccount.getDescription());

        // Account details
        if (virtualAccount.getAccountDetails() != null && !virtualAccount.getAccountDetails().isEmpty()) {
            List<Map<String, Object>> accountDetails = virtualAccount.getAccountDetails().stream()
                    .map(detail -> {
                        Map<String, Object> detailMap = new HashMap<>();
                        detailMap.put("id", detail.getExternalId());
                        detailMap.put("accountNumberSafe", detail.getAccountNumberSafe());
                        detailMap.put("accountNumberType", detail.getAccountNumberType());
                        return detailMap;
                    })
                    .collect(Collectors.toList());
            response.put("accountDetails", accountDetails);
        }

        // Routing details
        if (virtualAccount.getRoutingDetails() != null && !virtualAccount.getRoutingDetails().isEmpty()) {
            List<Map<String, Object>> routingDetails = virtualAccount.getRoutingDetails().stream()
                    .map(routing -> {
                        Map<String, Object> routingMap = new HashMap<>();
                        routingMap.put("id", routing.getExternalId());
                        routingMap.put("bankName", routing.getBankName());
                        routingMap.put("routingNumber", routing.getRoutingNumber());
                        routingMap.put("routingNumberType", routing.getRoutingNumberType());

                        // Bank address if available
                        if (routing.getBankAddress() != null) {
                            Map<String, Object> bankAddress = new HashMap<>();
                            bankAddress.put("line1", routing.getBankAddress().getLine1());
                            bankAddress.put("line2", routing.getBankAddress().getLine2());
                            bankAddress.put("locality", routing.getBankAddress().getLocality());
                            bankAddress.put("region", routing.getBankAddress().getRegion());
                            bankAddress.put("postalCode", routing.getBankAddress().getPostalCode());
                            bankAddress.put("country", routing.getBankAddress().getCountry());
                            routingMap.put("bankAddress", bankAddress);
                        }

                        return routingMap;
                    })
                    .collect(Collectors.toList());
            response.put("routingDetails", routingDetails);
        }

        return response;
    }

    /**
     *  Get onboarding summary for review page
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getOnboardingSummary(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            log.info("📋 Getting onboarding summary for user: {}", userId);

            OnboardingSummaryResponse summary = onboardingService.getOnboardingSummary(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", summary);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error getting onboarding summary: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to get onboarding summary: " + e.getMessage()));
        }
    }
    /**
     * Helper method to extract user ID from JWT token
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        try {
            // ✅ PRODUCTION: Extract userId directly from JWT
            if (authentication instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
                String token = (String) jwtAuth.getCredentials();

                // Extract userId directly from JWT
                Long userId = jwtUtil.extractUserId(token);
                log.debug("Extracted user ID {} from JWT token", userId);
                return userId;
            }

            throw new RuntimeException("Invalid authentication token type");

        } catch (Exception e) {
            log.error("Failed to extract user ID from authentication: {}", e.getMessage());
            throw new RuntimeException("Failed to extract user ID from authentication");
        }
    }

    /**
     * Calculate progress percentage based on current step
     */
    private int getProgressPercentage(OnboardingStatus.OnboardingStep step) {
        return switch (step) {
            case PHONE_VERIFICATION -> 10;
            case PERSONAL_INFO -> 20;
            case ADDRESS -> 30;
            case RENTAL_DATA -> 40;
            case PROPERTY_MANAGER -> 50;
            case IDENTITY_VERIFICATION -> 60;
            case PAYMENT_SCHEDULE -> 70;
            case BANK_CONNECTION -> 80;
            case VIRTUAL_ACCOUNT_SETUP -> 90;
            case COMPLETED -> 100;
        };
    }
}
