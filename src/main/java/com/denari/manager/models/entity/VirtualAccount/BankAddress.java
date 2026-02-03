package com.denari.manager.models.entity.VirtualAccount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.OffsetDateTime;

@Entity
@Data
@Table(name = "bank_addresses")
@EqualsAndHashCode(exclude = "routingDetail")
@ToString(exclude = "routingDetail")
public class BankAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_addresses_id")
    private Long id;

    @Column(name = "mt_bank_addresses_id")
    private String externalId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routing_detail_id")
    private RoutingDetail routingDetail;

    @Column(name = "line1")
    private String line1;

    @Column(name = "line2")
    private String line2;

    @Column(name = "object")
    private String object;

    @Column(name = "region")
    private String region;

    @Column(name = "country")
    private String country;

    @Column(name = "locality")
    private String locality;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "live_mode")
    private Boolean liveMode = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

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


