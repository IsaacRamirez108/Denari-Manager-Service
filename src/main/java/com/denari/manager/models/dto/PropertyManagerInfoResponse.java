package com.denari.manager.models.dto;

import lombok.Data;

@Data
public class PropertyManagerInfoResponse {
    private Long id;
    private String individualOrCompany;
    private String name;
    private String email;
    private String phoneNumber;
    private String propertyName;
}

