package com.denari.manager.services.Payments;

import com.denari.manager.models.entity.Payments.PaymentRecord;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentRetryService {

    /**
     * Determine if payment should be retried based on failure reason
     */
    public RetryDecision shouldRetryPayment(String failureReason, String failureCode) {
        String reason = failureReason != null ? failureReason.toLowerCase() : "";
        String code = failureCode != null ? failureCode.toLowerCase() : "";

        // NSF (insufficient funds) - retry once after 3 days
        if (reason.contains("insufficient") || reason.contains("nsf") ||
                code.contains("r01") || code.contains("r09")) {
            return RetryDecision.builder()
                    .shouldRetry(true)
                    .retryDelayDays(3)
                    .maxRetries(1)
                    .reason("NSF - retry after 3 days")
                    .build();
        }

        // Invalid account - don't retry, needs user action
        if (reason.contains("invalid") || reason.contains("closed") ||
                code.contains("r02") || code.contains("r03")) {
            return RetryDecision.builder()
                    .shouldRetry(false)
                    .reason("Invalid account - requires user to update account info")
                    .build();
        }

        // Unauthorized - don't retry, needs user authorization
        if (reason.contains("unauthorized") || reason.contains("revoked") ||
                code.contains("r05") || code.contains("r07")) {
            return RetryDecision.builder()
                    .shouldRetry(false)
                    .reason("Unauthorized - requires user authorization")
                    .build();
        }

        // Temporary bank issues - retry once after 1 day
        if (reason.contains("temporary") || reason.contains("processing") ||
                code.contains("r06") || code.contains("r20")) {
            return RetryDecision.builder()
                    .shouldRetry(true)
                    .retryDelayDays(1)
                    .maxRetries(1)
                    .reason("Temporary issue - retry after 1 day")
                    .build();
        }

        // Default: don't retry unknown errors
        return RetryDecision.builder()
                .shouldRetry(false)
                .reason("Unknown error - requires manual review")
                .build();
    }

    /**
     * Schedule payment retry (placeholder for actual implementation)
     */
    public void schedulePaymentRetry(PaymentRecord failedPayment, int retryDelayDays) {
        log.info("🔄 Scheduling payment retry:");
        log.info("🔄 - Payment ID: {}", failedPayment.getId());
        log.info("🔄 - User: {}", failedPayment.getUser().getId());
        log.info("🔄 - Retry in {} days", retryDelayDays);

        // TODO: Implement actual retry scheduling
        // - Create scheduled job to retry payment
        // - Update payment record with retry information
        // - Set up monitoring for retry success/failure
    }

    /**
     * ✅ MISSING CLASS: RetryDecision - This was causing your compilation error!
     */
    @Builder
    @Data
    public static class RetryDecision {
        private boolean shouldRetry;
        private int retryDelayDays;
        private int maxRetries;
        private String reason;

        // Helper methods
        public boolean shouldRetry() {
            return shouldRetry;
        }

        public int getRetryDelayDays() {
            return retryDelayDays;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public String getReason() {
            return reason;
        }

        // Static factory methods for common scenarios
        public static RetryDecision retry(int delayDays, String reason) {
            return RetryDecision.builder()
                    .shouldRetry(true)
                    .retryDelayDays(delayDays)
                    .maxRetries(1)
                    .reason(reason)
                    .build();
        }

        public static RetryDecision noRetry(String reason) {
            return RetryDecision.builder()
                    .shouldRetry(false)
                    .retryDelayDays(0)
                    .maxRetries(0)
                    .reason(reason)
                    .build();
        }
    }

    /**
     * Get retry statistics for monitoring
     */
    public RetryStatistics getRetryStatistics() {
        // TODO: Implement retry statistics tracking
        return new RetryStatistics();
    }

    /**
     * Simple stats class for retry monitoring
     */
    @Data
    public static class RetryStatistics {
        private long totalRetries = 0;
        private long successfulRetries = 0;
        private long failedRetries = 0;
        private long nsfRetries = 0;
        private long temporaryIssueRetries = 0;

        public double getSuccessRate() {
            return totalRetries > 0 ? (double) successfulRetries / totalRetries * 100 : 0.0;
        }
    }

    /**
     * Cancel scheduled retry
     */
    public boolean cancelScheduledRetry(Long paymentRecordId) {
        try {
            log.info("🚫 Cancelling scheduled retry for payment record: {}", paymentRecordId);

            // TODO: Implement retry cancellation
            // - Find scheduled retry job
            // - Cancel the job
            // - Update payment record status

            return true;

        } catch (Exception e) {
            log.error("❌ Error cancelling scheduled retry: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Mark retry as completed (success or failure)
     */
    public void markRetryCompleted(Long paymentRecordId, boolean successful, String reason) {
        try {
            log.info("📊 Marking retry as completed:");
            log.info("📊 - Payment Record ID: {}", paymentRecordId);
            log.info("📊 - Successful: {}", successful);
            log.info("📊 - Reason: {}", reason);

            // TODO: Implement retry completion tracking
            // - Update retry statistics
            // - Update payment record with retry result
            // - Send notifications if needed

        } catch (Exception e) {
            log.error("❌ Error marking retry as completed: {}", e.getMessage(), e);
        }
    }
}
