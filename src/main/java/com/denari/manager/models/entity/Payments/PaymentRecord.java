package com.denari.manager.models.entity.Payments;

import com.denari.manager.enums.PaymentStatus;
import com.denari.manager.enums.PaymentType;
import com.denari.manager.models.entity.User.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_records")
@Data
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Modern Treasury IDs for tracking
    @Column(name = "incoming_payment_id", nullable = false)
    private String incomingPaymentId; // The original rent payment from property manager

    @Column(name = "mt_payment_order_id", nullable = false)
    private String mtPaymentOrderId; // Our ACH debit to user

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    // Metadata
    @Column(name = "description")
    private String description;

    @Column(name = "statement_descriptor")
    private String statementDescriptor;

    // Month/year for reporting
    @Column(name = "rent_month")
    private String rentMonth; // Format: "2025-06"

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isCompleted() {
        return PaymentStatus.COMPLETED.equals(this.status);
    }

    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(this.status) ||
                PaymentStatus.RETURNED.equals(this.status);
    }

    public boolean isPending() {
        return PaymentStatus.SCHEDULED.equals(this.status) ||
                PaymentStatus.PENDING.equals(this.status);
    }

    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}

