package com.denari.manager.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "denari.payment")
@Data
public class PaymentConfig {

    /**
     * Denari company account ID in Modern Treasury
     */
    @NotBlank(message = "Company account ID is required")
    private String companyAccountId;

    /**
     * Maximum payment amount allowed
     */
    @NotNull(message = "Max payment amount is required")
    @Positive(message = "Max payment amount must be positive")
    private BigDecimal maxPaymentAmount = new BigDecimal("10000.00");

    /**
     * Service fee amount (in dollars)
     */
    @NotNull(message = "Service fee is required")
    private BigDecimal serviceFee = new BigDecimal("15.00");

    /**
     * Maximum days in future for effective date
     */
    @Positive(message = "Max future days must be positive")
    private int maxFutureDays = 60;

    /**
     * Payment retry configuration
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * Notification configuration
     */
    private NotificationConfig notification = new NotificationConfig();

    @Data
    public static class RetryConfig {
        /**
         * Maximum number of retry attempts
         */
        @Positive(message = "Max retry attempts must be positive")
        private int maxAttempts = 3;

        /**
         * Initial retry delay in days
         */
        @Positive(message = "Initial retry delay must be positive")
        private int initialDelayDays = 1;

        /**
         * Retry delay for NSF scenarios
         */
        @Positive(message = "NSF retry delay must be positive")
        private int nsfRetryDelayDays = 3;

        /**
         * Enable automatic retries
         */
        private boolean enabled = true;
    }

    @Data
    public static class NotificationConfig {
        /**
         * Enable email notifications
         */
        private boolean emailEnabled = true;

        /**
         * Enable SMS notifications
         */
        private boolean smsEnabled = false;

        /**
         * Enable push notifications
         */
        private boolean pushEnabled = false;

        /**
         * Send notifications for successful payments
         */
        private boolean notifyOnSuccess = true;

        /**
         * Send notifications for failed payments
         */
        private boolean notifyOnFailure = true;

        /**
         * Send notifications for rent cycle completion
         */
        private boolean notifyOnCycleComplete = true;
    }

    // Helper methods
    public boolean isValidPaymentAmount(BigDecimal amount) {
        return amount != null &&
                amount.compareTo(BigDecimal.ZERO) > 0 &&
                amount.compareTo(maxPaymentAmount) <= 0;
    }

    public BigDecimal calculateSecondPaymentAmount(BigDecimal firstPaymentAmount) {
        return firstPaymentAmount.add(serviceFee);
    }

    public BigDecimal calculateTotalMonthlyCharge(BigDecimal monthlyRent) {
        return monthlyRent.add(serviceFee);
    }
}
