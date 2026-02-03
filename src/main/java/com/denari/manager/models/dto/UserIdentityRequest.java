package com.denari.manager.models.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class UserIdentityRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?1?\\d{10}$", message = "Phone number must be valid US format")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "SSN is required")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{4}$", message = "SSN must be in format XXX-XX-XXXX")
    private String ssn;
}
