package com.denari.manager.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    SCHEDULED("Payment scheduled for future date"),
    PENDING("Payment submitted to bank"),
    COMPLETED("Payment successfully processed"),
    FAILED("Payment failed to process"),
    CANCELLED("Payment cancelled before processing"),
    RETURNED("Payment returned by bank after initial success");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == RETURNED;
    }

    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    public boolean requiresAction() {
        return this == FAILED || this == RETURNED;
    }
}
