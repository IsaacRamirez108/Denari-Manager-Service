package com.denari.manager.models.dto;

import com.denari.manager.enums.PaymentType;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class PaymentOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @DecimalMax(value = "10000.00", message = "Amount cannot exceed $10,000")
    private BigDecimal amount;

    @NotNull(message = "Effective date is required")
    @Future(message = "Effective date must be in the future")
    private LocalDate effectiveDate;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @Size(max = 22, message = "Statement descriptor cannot exceed 22 characters")
    private String statementDescriptor;

    // Optional metadata
    private Map<String, String> metadata;

    // Optional priority (normal, high)
    private String priority = "normal";

    // Optional transaction monitoring
    private Boolean transactionMonitoringEnabled = true;
}
