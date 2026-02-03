package com.denari.manager.models.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OnboardingSummaryResponse {

    // User Information
    private UserSummary user;

    // Address Information
    private AddressSummary address;

    // Rental Information
    private RentalSummary rental;

    // Property Manager Information
    private PropertyManagerSummary propertyManager;

    // Payment Schedule Information
    private PaymentScheduleSummary paymentSchedule;

    // Bank Account Information (if connected)
    private BankAccountSummary bankAccount;

    // Virtual Account Information (if available)
    private VirtualAccountSummary virtualAccount;

    @Data
    public static class UserSummary {
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
    }

    @Data
    public static class AddressSummary {
        private String street;
        private String apartmentNumber;
        private String city;
        private String state;
        private String postalCode;
        private String fullAddress;
    }

    @Data
    public static class RentalSummary {
        private BigDecimal monthlyRent;
        private String moveInDate;
        private String moveOutDate;
        private String currency;
    }

    @Data
    public static class PropertyManagerSummary {
        private String name;
        private String email;
        private String phoneNumber;
        private String propertyName;
    }

    @Data
    public static class PaymentScheduleSummary {
        private Integer firstPaymentDate;
        private Integer secondPaymentDate;
        private BigDecimal firstPaymentAmount;
        private BigDecimal secondPaymentAmount;
        private BigDecimal serviceFee;
        private BigDecimal totalMonthlyCharge;
        private BigDecimal monthlyRent;
    }

    @Data
    public static class BankAccountSummary {
        private String accountName;
        private String bankName;
        private String accountNumberLastFour;
        private String accountType;
        private String verificationStatus;
    }

    @Data
    public static class VirtualAccountSummary {
        private String accountName;
        private String accountNumberSafe;
        private String routingNumber;
        private String bankName;
        private Boolean active;
    }
}
