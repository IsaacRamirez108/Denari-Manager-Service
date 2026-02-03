package com.denari.manager.models.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class PaymentScheduleRequest {
    @Min(value = 1, message = "First payment date must be between 1 and 31")
    @Max(value = 31, message = "First payment date must be between 1 and 31")
    private Integer firstPaymentDate;

    @Min(value = 1, message = "Second payment date must be between 1 and 31")
    @Max(value = 31, message = "Second payment date must be between 1 and 31")
    private Integer secondPaymentDate;
}
