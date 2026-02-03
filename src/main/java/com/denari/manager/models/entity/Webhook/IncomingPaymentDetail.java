package com.denari.manager.models.entity.Webhook;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "incoming_payment_details")
public class IncomingPaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incoming_payment_details_id")
    private Long id;
    @Column(name = "incoming_payment_details_external_id")
    private String externalId;
    @Column(name = "internal_account_id")
    private String internalAccountId;
    @Column(name = "virtual_account_id")
    private String virtualAccountId;
    @Column(name = "ledger_transaction_id")
    private String ledgerTransactionId;
    @Column(nullable = false)
    private String event;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "currency")
    private String currency;
    @Column(name = "status")
    private String status;
    @Column(name = "direction")
    private String direction;
    @Column(name = "employer_name")
    private String employerName;
    @Column(name = "trace_number")
    private String traceNumber;
    @Column(name = "payment_Related_info")
    private String paymentRelatedInfo;
    @Column(name = "as_of_date")
    private LocalDate asOfDate;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
