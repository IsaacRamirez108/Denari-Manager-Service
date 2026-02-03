package com.denari.manager.models.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AddressRequest {
    @NotBlank(message = "Street address is required")
    @Size(min = 5, max = 100, message = "Street address must be between 5 and 100 characters")
    private String street;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 2, message = "State must be 2 characters")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Postal code must be valid US format")
    private String postalCode;

    @Size(max = 20, message = "Apartment number must not exceed 20 characters")
    private String apartmentNumber;
}
