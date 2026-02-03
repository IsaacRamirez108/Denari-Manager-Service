package com.denari.manager.models.entity.VirtualAccount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.OffsetDateTime;

@Entity
@Data
@Table(name = "routing_details")
@EqualsAndHashCode(exclude = {"virtualAccount", "bankAddress"})
@ToString(exclude = {"virtualAccount", "bankAddress"})
public class RoutingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routing_details_id")
    private Long id;

    @Column(name = "mt_routing_details_id")
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virtual_account_id", referencedColumnName = "virtual_account_id")
    private VirtualAccount virtualAccount;

    @OneToOne(mappedBy = "routingDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BankAddress bankAddress;

    @Column(name = "object")
    private String object;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "live_mode")
    private Boolean liveMode = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Essential routing fields
    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "routing_number")
    private String routingNumber;

    @Column(name = "routing_number_type")
    private String routingNumberType;

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

