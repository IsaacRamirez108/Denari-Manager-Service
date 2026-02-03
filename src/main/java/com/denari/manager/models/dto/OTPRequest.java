package com.denari.manager.models.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class OTPRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?1?\\d{10}$", message = "Phone number must be valid")
    private String phoneNumber;
}
