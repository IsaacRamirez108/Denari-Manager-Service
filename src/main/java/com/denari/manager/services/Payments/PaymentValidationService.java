package com.denari.manager.services.Payments;

import com.denari.manager.enums.PaymentType;
import com.denari.manager.models.entity.User.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Slf4j
public class PaymentValidationService {

    /**
     * Validate payment request before creating payment order
     */
    public void validatePaymentRequest(User user, BigDecimal amount, LocalDate effectiveDate,
                                       PaymentType paymentType) {
        // Validate user
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        if (amount.compareTo(new BigDecimal("10000")) > 0) { // Max $10,000
            throw new IllegalArgumentException("Payment amount exceeds maximum limit");
        }

        // Validate effective date
        if (effectiveDate == null) {
            throw new IllegalArgumentException("Effective date cannot be null");
        }

        if (effectiveDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Effective date cannot be in the past");
        }

        if (effectiveDate.isAfter(LocalDate.now().plusDays(60))) {
            throw new IllegalArgumentException("Effective date cannot be more than 60 days in the future");
        }

        // Validate payment type
        if (paymentType == null) {
            throw new IllegalArgumentException("Payment type cannot be null");
        }

        log.info("✅ Payment validation passed for user: {}", user.getId());
    }
}
