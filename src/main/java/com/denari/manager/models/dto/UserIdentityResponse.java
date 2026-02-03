package com.denari.manager.models.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserIdentityResponse {
    private Long id;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String ssnMasked;
    private Boolean phoneVerified;
    private Boolean identityVerified;
    private LocalDateTime otpExpiresAt;
}
