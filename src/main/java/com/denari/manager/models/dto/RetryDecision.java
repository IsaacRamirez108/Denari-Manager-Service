package com.denari.manager.models.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RetryDecision {
    private boolean shouldRetry;
    private int retryDelayDays;
    private int maxRetries;
    private String reason;
}
