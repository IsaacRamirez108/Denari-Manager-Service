package com.denari.manager.models.entity.User;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_history")
@Data
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class PaymentHistory {

    @Id
    private String paymentId;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Column(name = "currency", nullable = false)
    private String currency = "USD";

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;


    public enum PaymentStatus {
        COMPLETED, PENDING, FAILED, SCHEDULED
    }

    public enum PaymentType {
        FIRST_PAYMENT, SECOND_PAYMENT_WITH_FEE
    }
}
