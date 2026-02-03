package com.denari.manager.models.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentScheduleResponse {
    private Long id;
    private Integer firstPaymentDate;
    private Integer secondPaymentDate;
    private BigDecimal firstPaymentAmount;
    private BigDecimal secondPaymentAmount;
    private BigDecimal serviceFee;
    private String status;
}
