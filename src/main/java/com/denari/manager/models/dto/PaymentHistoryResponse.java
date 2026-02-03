package com.denari.manager.models.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentHistoryResponse {
    private String paymentId;
    private BigDecimal amount; // Convert from cents
    private String currency;
    private LocalDate dueDate;
    private LocalDateTime paidAt;
    private String status;
    private String paymentType;
    private Boolean isOverdue;

    // Computed field
    public Boolean getIsOverdue() {
        return "PENDING".equals(status) && dueDate != null && dueDate.isBefore(LocalDate.now());
    }
}
