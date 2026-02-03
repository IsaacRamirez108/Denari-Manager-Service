package com.denari.manager.models.entity.ExternalAccount;

import com.denari.manager.models.entity.User.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "external_bank_accounts")
@Data
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class ExternalBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mt_external_account_id", unique = true)
    private String mtExternalAccountId;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_number_last_four", nullable = false)
    private String accountNumberLastFour;

    @Column(name = "routing_number", nullable = false)
    private String routingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum AccountType {
        CHECKING, SAVINGS
    }

    public enum VerificationStatus {
        PENDING, VERIFIED, FAILED
    }
}
