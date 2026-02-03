package com.denari.manager.models.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RentalDataRequest {
    @NotNull(message = "Form payment is required")
    private String formPayment;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "100.00", message = "Monthly rent must be at least $100")
    @DecimalMax(value = "50000.00", message = "Monthly rent cannot exceed $50,000")
    private BigDecimal monthlyRent;

    private String currency = "USD";

    private LocalDate moveInDate;

    @Future(message = "Move-out date must be in the future")
    private LocalDate moveOutDate;

    @Min(value = 1, message = "Rent due date must be between 1 and 31")
    @Max(value = 31, message = "Rent due date must be between 1 and 31")
    private Integer rentDueDate;
}
