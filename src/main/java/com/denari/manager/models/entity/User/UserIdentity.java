package com.denari.manager.models.entity.User;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_identities")
@Data
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "ssn_encrypted")
    private String ssnEncrypted;

    @Column(name = "ssn_masked")
    private String ssnMasked;

    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;

    @Column(name = "last_otp")
    private String lastOtp;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

}

