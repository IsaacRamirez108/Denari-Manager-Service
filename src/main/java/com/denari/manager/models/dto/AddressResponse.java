package com.denari.manager.models.dto;

import lombok.Data;

@Data
public class AddressResponse {
    private Long id;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String apartmentNumber;

    // Computed field
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(street);
        if (apartmentNumber != null && !apartmentNumber.isEmpty()) {
            sb.append(", ").append(apartmentNumber);
        }
        sb.append(", ").append(city).append(", ").append(state).append(" ").append(postalCode);
        return sb.toString();
    }
}
