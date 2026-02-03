package com.denari.manager.models.entity.VirtualAccount;

import com.denari.manager.models.entity.User.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "virtual_accounts")
@EqualsAndHashCode(exclude = {"user", "accountDetails", "routingDetails"})
@ToString(exclude = {"user", "accountDetails", "routingDetails"})
public class VirtualAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "virtual_account_id")
    private Long id;

    @Column(name = "mt_virtual_account_id", unique = true)
    private String mtId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "name")
    private String name;

    @Column(name = "object")
    private String object;

    @Column(name = "live_mode")
    private Boolean liveMode = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "description")
    private String description;

    // Essential fields only
    @Column(name = "counterparty_id")
    private String counterpartyId;

    @Column(name = "internal_account_id")
    private String internalAccountId;

    @Column(name = "debit_ledger_account_id")
    private String debitLedgerAccountId;

    @Column(name = "credit_ledger_account_id")
    private String creditLedgerAccountId;

    @OneToMany(mappedBy = "virtualAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccountDetail> accountDetails;

    @OneToMany(mappedBy = "virtualAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoutingDetail> routingDetails;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
