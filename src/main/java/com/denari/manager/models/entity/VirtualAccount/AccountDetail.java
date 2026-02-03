package com.denari.manager.models.entity.VirtualAccount;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@Table(name = "account_details")
@EqualsAndHashCode(exclude = "virtualAccount")
@ToString(exclude = "virtualAccount")
public class AccountDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_details_id")
    private Long id;

    @Column(name = "mt_account_details_id")
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virtual_account_id", referencedColumnName = "virtual_account_id")
    private VirtualAccount virtualAccount;

    @Column(name = "object")
    private String object;

    @Column(name = "live_mode")
    private Boolean liveMode = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Essential fields
    @Column(name = "account_number_safe")
    private String accountNumberSafe;

    @Column(name = "account_number_type")
    private String accountNumberType;

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
