package com.denari.manager.models.entity.User;

import com.denari.manager.enums.PaymentScheduleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_schedules")
@Data
@EqualsAndHashCode(exclude = "rentalData")
@ToString(exclude = "rentalData")
public class PaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_payment_date", nullable = false)
    private Integer firstPaymentDate;

    @Column(name = "second_payment_date", nullable = false)
    private Integer secondPaymentDate;

    // ✅ FIXED: Removed "Cents" from field names as requested
    @Column(name = "first_payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal firstPaymentAmount;

    @Column(name = "second_payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal secondPaymentAmount;

    @Column(name = "service_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal serviceFee;

    // ✅ NEW: Added total rent field as requested
    @Column(name = "total_monthly_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalMonthlyCharge;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentScheduleStatus status;

    @OneToOne
    @JoinColumn(name = "rental_data_id", nullable = false)
    private RentalData rentalData;

    // ✅ HELPER METHODS for calculations
    public void calculatePayments(BigDecimal monthlyRent) {
        BigDecimal serviceFeeAmount = new BigDecimal("15.00"); // $15 service fee
        BigDecimal halfRent = monthlyRent.divide(new BigDecimal("2"));

        this.firstPaymentAmount = halfRent;
        this.secondPaymentAmount = halfRent.add(serviceFeeAmount); // Add service fee to second payment
        this.serviceFee = serviceFeeAmount;
        this.totalMonthlyCharge = monthlyRent.add(serviceFeeAmount); // Total rent + service fee
    }
}
