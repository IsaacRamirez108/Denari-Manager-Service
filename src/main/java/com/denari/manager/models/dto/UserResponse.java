package com.denari.manager.models.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String status;
    private LocalDateTime createdAt;

    // Navigation properties (IDs only for security)
    private Long userIdentityId;
    private String onboardingCurrentStep;
    private Long rentalDataId;
    private Long externalBankAccountId;
    private Long virtualAccountId;
}

