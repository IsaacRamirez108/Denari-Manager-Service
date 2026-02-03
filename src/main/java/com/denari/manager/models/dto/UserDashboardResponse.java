package com.denari.manager.models.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UserDashboardResponse {
    private UserResponse user;
    private OnboardingStatusResponse onboardingStatus;
    private RentalDataResponse rentalData;
    private List<PaymentHistoryResponse> upcomingPayments;
    private List<PaymentHistoryResponse> recentPayments;
    private ExternalBankAccountResponse primaryBankAccount;
    private VirtualAccountResponse virtualAccount;

    // Computed dashboard metrics
    private BigDecimal totalMonthlyCharge; // Rent + service fee
    private Integer daysUntilNextPayment;
    private String accountStatus; // COMPLETE, PENDING_SETUP, etc.
}

