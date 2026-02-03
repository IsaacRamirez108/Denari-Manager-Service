package com.denari.manager.models.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccountDetailResponse {
    private Long id;
    private Boolean liveMode;
    private LocalDateTime createdAt;
    private String accountNumberSafe;
    private String accountNumberType;
}
