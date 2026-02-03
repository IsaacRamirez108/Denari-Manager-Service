package com.denari.manager.models.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ExternalBankAccountRequest {
    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
    private String accountName;

    @NotNull(message = "Account type is required")
    private String accountType;

    @NotBlank(message = "Bank name is required")
    @Size(min = 2, max = 100, message = "Bank name must be between 2 and 100 characters")
    private String bankName;

    @NotBlank(message = "Account number is required")
    @Size(min = 4, max = 20, message = "Account number must be between 4 and 20 characters")
    private String accountNumber;

    @NotBlank(message = "Routing number is required")
    @Pattern(regexp = "^\\d{9}$", message = "Routing number must be 9 digits")
    private String routingNumber;
}
