package com.denari.manager.enums;

import lombok.Getter;

@Getter
public enum PaymentType {
    FIRST_PAYMENT,   // First 50% of rent (on 1st)
    SECOND_PAYMENT,  // Second 50% + service fee (on chosen date)
    SERVICE_FEE,     // If tracked separately
    REFUND          // For refund scenarios
}
