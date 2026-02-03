package com.denari.manager.models.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserResponse user;
    private String onboardingStatus;
    private Boolean requiresOnboarding;
}
