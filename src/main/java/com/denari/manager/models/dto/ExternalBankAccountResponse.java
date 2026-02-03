package com.denari.manager.models.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExternalBankAccountResponse {
    private Long id;
    private String accountName;
    private String accountType;
    private String bankName;
    private String accountNumberLastFour; // Only last 4 digits for security
    private String verificationStatus;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
    // Note: Full routing number is NOT included for security
}
