package com.denari.manager.models.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class OnboardingStatusResponse {
    private Long id;
    private String currentStep;
    private Set<String> completedSteps;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Double progressPercentage;
}
