package com.denari.manager.services;

import com.denari.manager.models.dto.OTPData;
import com.denari.manager.models.entity.User.OnboardingStatus;
import com.denari.manager.models.entity.User.User;
import com.denari.manager.models.entity.User.UserIdentity;
import com.denari.manager.repositories.OnboardingStatusRepository;
import com.denari.manager.repositories.UserIdentityRepository;
import com.denari.manager.repositories.UserRepository;
import com.denari.manager.security.JwtUtil;
import com.denari.manager.util.PhoneNumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private SmsService smsService;

    @Autowired
    private PhoneNumberUtil phoneNumberUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Autowired
    private OnboardingStatusRepository onboardingStatusRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.environment:development}")
    private String environment;

    // Keep in-memory store for debugging/logging (Twilio handles actual OTP)
    private final ConcurrentHashMap<String, OTPData> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Send OTP to phone number using Twilio Verify
     */
    public boolean sendOtp(String phoneNumber) {
        try {
            String normalizedPhone = phoneNumberUtil.normalizePhoneNumber(phoneNumber);

            boolean isValid = "production".equals(environment)
                    ? phoneNumberUtil.isValidUsPhoneNumber(normalizedPhone)
                    : phoneNumberUtil.isValidUsPhoneNumberLenient(normalizedPhone);

            if (!isValid) {
                log.warn("Invalid phone number format: {} (environment: {})", phoneNumber, environment);
                return false;
            }

            // Generate OTP for logging purposes (Twilio generates the real one)
            String debugOtp = String.format("%06d", secureRandom.nextInt(10000));
            otpStore.put(normalizedPhone, new OTPData(normalizedPhone, debugOtp));

            // Use Twilio Verify API (ignores debugOtp, generates its own)
            boolean smsSent = smsService.sendOtpSms(normalizedPhone, debugOtp);

            if (smsSent) {
                log.info("OTP request sent successfully to {}",
                        normalizedPhone.replaceAll("\\d(?=\\d{4})", "*"));
                cleanupExpiredOtps();
                return true;
            } else {
                otpStore.remove(normalizedPhone);
                return false;
            }

        } catch (Exception e) {
            log.error("Error sending OTP to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    /**
     * Verify OTP using Twilio Verify
     */
    @Transactional
    public String verifyOtp(String phoneNumber, String otpCode) {
        try {
            String normalizedPhone = phoneNumberUtil.normalizePhoneNumber(phoneNumber);

            // Use Twilio Verify API for actual verification
            boolean isValidOtp = smsService.verifyOtp(normalizedPhone, otpCode);

            if (!isValidOtp) {
                log.warn("Invalid OTP for phone: {}", normalizedPhone.replaceAll("\\d(?=\\d{4})", "*"));
                return null;
            }

            // Clean up debug data
            otpStore.remove(normalizedPhone);

            // Find or create user
            User user = findOrCreateUser(normalizedPhone);

            // ✅ PRODUCTION FIX: Generate JWT token with userId and email
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());

            log.info("OTP verified successfully for user: {}", user.getId());
            return token;

        } catch (Exception e) {
            log.error("Error verifying OTP for {}: {}", phoneNumber, e.getMessage());
            return null;
        }
    }

    /**
     * Find existing user or create new user after phone verification
     */
    @Transactional
    public User findOrCreateUser(String normalizedPhone) {
        // Try to find existing user by phone
        Optional<UserIdentity> existingIdentity = userIdentityRepository.findByPhoneNumber(normalizedPhone);

        if (existingIdentity.isPresent()) {
            // Update phone verification status
            UserIdentity identity = existingIdentity.get();
            identity.setPhoneVerified(true);
            identity.setLastOtp(null); // Clear stored OTP
            identity.setOtpExpiresAt(null);
            userIdentityRepository.save(identity);

            log.info("Found existing user for phone: {}", normalizedPhone.replaceAll("\\d(?=\\d{4})", "*"));
            return identity.getUser();
        } else {
            // Create new user with temporary email
            User newUser = new User();
            newUser.setFirstName("New"); // Temporary - will be updated in onboarding
            newUser.setLastName("User"); // Temporary - will be updated in onboarding
            newUser.setEmail("temp_" + System.currentTimeMillis() + "@denari.temp"); // Temporary unique email
            newUser.setStatus(User.UserStatus.PENDING);

            User savedUser = userRepository.save(newUser);

            // Create user identity
            UserIdentity newIdentity = new UserIdentity();
            newIdentity.setUser(savedUser);
            newIdentity.setPhoneNumber(normalizedPhone);
            newIdentity.setPhoneVerified(true);
            userIdentityRepository.save(newIdentity);

            // Create onboarding status
            OnboardingStatus onboardingStatus = new OnboardingStatus();
            onboardingStatus.setUser(savedUser);
            onboardingStatus.setCurrentStep(OnboardingStatus.OnboardingStep.PERSONAL_INFO);
            onboardingStatus.setStartedAt(LocalDateTime.now());
            onboardingStatusRepository.save(onboardingStatus);

            log.info("Created new user for phone: {}", normalizedPhone.replaceAll("\\d(?=\\d{4})", "*"));
            return savedUser;
        }
    }

    /**
     * Get user by JWT token
     */
    public User getCurrentUser(String token) {
        try {
            String email = jwtUtil.extractUsername(token);
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            log.error("Error getting current user from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get user by email
     */
    public User getCurrentUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            log.error("Error getting current user by email: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Clean up expired OTPs from memory
     */
    private void cleanupExpiredOtps() {
        otpStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Get onboarding status for user
     */
    public OnboardingStatus.OnboardingStep getCurrentOnboardingStep(Long userId) {
        Optional<OnboardingStatus> status = onboardingStatusRepository.findByUserId(userId);
        return status.map(OnboardingStatus::getCurrentStep)
                .orElse(OnboardingStatus.OnboardingStep.PHONE_VERIFICATION);
    }
}

