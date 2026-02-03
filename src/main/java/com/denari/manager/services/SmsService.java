package com.denari.manager.services;

import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.verify-service-sid}")
    private String verifyServiceSid;

    @Value("${app.name:Denari}")
    private String appName;

    /**
     * Send OTP using Twilio Verify API
     */
    public boolean sendOtpSms(String phoneNumber, String otpCode) {
        try {
            log.info("Sending OTP via Twilio Verify to: {}",
                    phoneNumber.replaceAll("\\d(?=\\d{4})", "*"));

            // Twilio Verify handles OTP generation, we ignore the passed otpCode
            Verification verification = Verification.creator(
                    verifyServiceSid,
                    phoneNumber,
                    "sms"
            ).create();

            log.info("Twilio Verify SMS sent successfully. SID: {}, Status: {}",
                    verification.getSid(), verification.getStatus());

            return "pending".equals(verification.getStatus());

        } catch (Exception e) {
            log.error("Failed to send Twilio Verify SMS to {}: {}",
                    phoneNumber.replaceAll("\\d(?=\\d{4})", "*"),
                    e.getMessage());
            return false;
        }
    }

    /**
     * Verify OTP using Twilio Verify API
     */
    public boolean verifyOtp(String phoneNumber, String otpCode) {
        try {
            log.info("Verifying OTP via Twilio Verify for: {}",
                    phoneNumber.replaceAll("\\d(?=\\d{4})", "*"));

            VerificationCheck verificationCheck = VerificationCheck.creator(
                            verifyServiceSid  // Only the service SID as parameter
                    )
                    .setTo(phoneNumber)      // Set the phone number
                    .setCode(otpCode)        // Set the OTP code
                    .create();

            log.info("Twilio Verify check result: {}", verificationCheck.getStatus());

            return "approved".equals(verificationCheck.getStatus());

        } catch (Exception e) {
            log.error("Failed to verify OTP for {}: {}",
                    phoneNumber.replaceAll("\\d(?=\\d{4})", "*"),
                    e.getMessage());
            return false;
        }
    }

    /**
     * Check if phone number is in valid format for Twilio
     */
    public boolean isValidPhoneFormat(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\+[1-9]\\d{1,14}$");
    }
}
