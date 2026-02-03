package com.denari.manager.models.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfile {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String maskedSSN;        // ✅ Safe to display
    private LocalDate dateOfBirth;
    private String status;
}
