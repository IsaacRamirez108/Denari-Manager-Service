package com.denari.manager.models.entity.User;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "rental_data")
@Data
@EqualsAndHashCode(exclude = {"user", "paymentSchedule", "address", "propertyManagerInfo"})
@ToString(exclude = {"user", "paymentSchedule", "address", "propertyManagerInfo"})
public class RentalData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "form_payment", nullable = false)
    private FormPayment formPayment;

    @Column(name = "monthly_rent_cents", nullable = false)
    private Long monthlyRentCents;

    @Column(name = "currency", nullable = false)
    private String currency = "USD";

    @Column(name = "move_in_date")
    private LocalDate moveInDate;

    @Column(name = "move_out_date")
    private LocalDate moveOutDate;

    @Column(name = "rent_due_date", nullable = false)
    private Integer rentDueDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @OneToOne(mappedBy = "rentalData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PaymentSchedule paymentSchedule;

    @OneToOne(mappedBy = "rentalData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserAddress userAddress;

    @OneToOne(mappedBy = "rentalData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PropertyManagerInfo propertyManagerInfo;

    public enum FormPayment {
        ACH, WIRE, CHECK, CREDIT_CARD
    }
}
