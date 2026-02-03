package com.denari.manager.controllers;

import com.denari.manager.models.dto.OTPRequest;
import com.denari.manager.models.dto.OTPVerificationRequest;
import com.denari.manager.models.dto.AuthResponse;
import com.denari.manager.models.dto.UserResponse;
import com.denari.manager.models.entity.User.OnboardingStatus;
import com.denari.manager.models.entity.User.User;
import com.denari.manager.services.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Send OTP to phone number
     */
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OTPRequest request) {
        log.info("OTP request for phone: {}", request.getPhoneNumber().replaceAll("\\d(?=\\d{4})", "*"));

        boolean otpSent = authService.sendOtp(request.getPhoneNumber());

        if (otpSent) {
            return ResponseEntity.ok().body(new ApiResponse("OTP sent successfully", true));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Failed to send OTP", false));
        }
    }

    /**
     * Verify OTP and return JWT token
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OTPVerificationRequest request) {
        log.info("OTP verification for phone: {}", request.getPhoneNumber().replaceAll("\\d(?=\\d{4})", "*"));

        String token = authService.verifyOtp(request.getPhoneNumber(), request.getOtp());

        if (token != null) {
            // Get user info
            User user = authService.getCurrentUser(token);
            UserResponse userResponse = modelMapper.map(user, UserResponse.class);

            // Get onboarding status
            OnboardingStatus.OnboardingStep currentStep = authService.getCurrentOnboardingStep(user.getId());
            boolean requiresOnboarding = currentStep != OnboardingStatus.OnboardingStep.COMPLETED;

            // Build auth response
            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setTokenType("Bearer");
            authResponse.setExpiresIn(86400L); // 24 hours
            authResponse.setUser(userResponse);
            authResponse.setOnboardingStatus(currentStep.toString());
            authResponse.setRequiresOnboarding(requiresOnboarding);

            return ResponseEntity.ok(authResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Invalid or expired OTP", false));
        }
    }

    /**
     * Get current authenticated user
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = authService.getCurrentUserByEmail(email);

            if (user != null) {
                UserResponse userResponse = modelMapper.map(user, UserResponse.class);
                OnboardingStatus.OnboardingStep currentStep = authService.getCurrentOnboardingStep(user.getId());
                userResponse.setOnboardingCurrentStep(currentStep.toString());

                return ResponseEntity.ok(userResponse);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse("User not found", false));
            }
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Internal server error", false));
        }
    }

    // Helper class for API responses
    public static class ApiResponse {
        private String message;
        private boolean success;

        public ApiResponse(String message, boolean success) {
            this.message = message;
            this.success = success;
        }

        // Getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}
