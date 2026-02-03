package com.denari.manager.models.entity.Webhook;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "payment_orders")
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_order_id")
    private long id;

    @Column(name = "payment_order_external__id", nullable = false, unique = true)
    private String externalPaymentOrderId;

    @Column(nullable = false)
    private String event;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "object", nullable = false)
    private String object;
}

