package com.denari.manager.models.dto;


import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class OTPVerificationRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?1?\\d{10}$", message = "Phone number must be valid")
    private String phoneNumber;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otp;
}
