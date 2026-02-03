package com.denari.manager.models.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PaymentSummaryResponse {
    private BigDecimal monthlyRent;
    private BigDecimal serviceFee;
    private BigDecimal totalMonthlyCharge;
    private Integer firstPaymentDate;
    private Integer secondPaymentDate;
    private LocalDate nextPaymentDue;
    private BigDecimal nextPaymentAmount;
    private List<PaymentHistoryResponse> upcomingPayments;
    private List<PaymentHistoryResponse> paymentHistory;
}
