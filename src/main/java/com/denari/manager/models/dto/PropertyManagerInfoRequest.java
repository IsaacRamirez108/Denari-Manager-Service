package com.denari.manager.models.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class PropertyManagerInfoRequest {
    @NotNull(message = "Individual or company type is required")
    private String individualOrCompany;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?1?\\d{10}$", message = "Phone number must be valid US format")
    private String phoneNumber;

    @Size(max = 100, message = "Property name must not exceed 100 characters")
    private String propertyName;
}
