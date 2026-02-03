package com.denari.manager.models.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OTPData {
    private String phoneNumber;
    private String otpCode;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public OTPData(String phoneNumber, String otpCode) {
        this.phoneNumber = phoneNumber;
        this.otpCode = otpCode;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(5);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

