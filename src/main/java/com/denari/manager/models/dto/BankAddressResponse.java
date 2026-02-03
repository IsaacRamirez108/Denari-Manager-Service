package com.denari.manager.models.dto;

import lombok.Data;

@Data
public class BankAddressResponse {
    private Long id;
    private String line1;
    private String line2;
    private String region;
    private String country;
    private String locality;
    private String postalCode;
}
