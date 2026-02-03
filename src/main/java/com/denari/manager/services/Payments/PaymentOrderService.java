package com.denari.manager.services.Payments;

import com.denari.manager.enums.PaymentType;
import com.denari.manager.models.entity.ExternalAccount.ExternalBankAccount;
import com.denari.manager.models.entity.User.User;
import com.denari.manager.repositories.ExternalBankAccountRepository;
import com.denari.manager.services.ModernTreasuryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentOrderService {

    private final ModernTreasuryService modernTreasuryService;
    private final ExternalBankAccountRepository externalBankAccountRepository;
    private final PaymentValidationService paymentValidationService;

    @Value("${denari.company.account.id}")
    private String companyAccountId;

    /**
     * Create ACH payment order for user rent payment
     */
    public String createPaymentOrder(User user, BigDecimal amount, LocalDate effectiveDate,
                                     PaymentType paymentType, String description) {
        try {
            log.info("💳 Creating ACH payment order:");
            log.info("💳 - User: {} {} (ID: {})", user.getFirstName(), user.getLastName(), user.getId());
            log.info("💳 - Type: {}", paymentType);
            log.info("💳 - Amount: ${}", amount);
            log.info("💳 - Effective Date: {}", effectiveDate);

            // Validate payment request
            paymentValidationService.validatePaymentRequest(user, amount, effectiveDate, paymentType);

            // Get user's bank account
            ExternalBankAccount bankAccount = getUserBankAccount(user);

            // Build payment order request
            Map<String, Object> paymentOrderRequest = buildPaymentOrderRequest(
                    user, bankAccount, amount, effectiveDate, paymentType, description);

            // Create payment order via Modern Treasury
            String paymentOrderId = modernTreasuryService.createPaymentOrder(paymentOrderRequest);

            log.info("✅ ACH payment order created successfully:");
            log.info("✅ - Payment Order ID: {}", paymentOrderId);
            log.info("✅ - User: {}", user.getId());
            log.info("✅ - Amount: ${}", amount);

            return paymentOrderId;

        } catch (Exception e) {
            log.error("❌ Failed to create payment order for user {}: {}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create payment order: " + e.getMessage());
        }
    }

    /**
     * Get user's external bank account for ACH debits
     */
    private ExternalBankAccount getUserBankAccount(User user) {
        Optional<ExternalBankAccount> bankAccount = externalBankAccountRepository.findByUser(user);

        if (bankAccount.isEmpty()) {
            throw new RuntimeException("No bank account found for user: " + user.getId());
        }

        ExternalBankAccount account = bankAccount.get();

        // Validate account is active and verified
//        if (!account.isActive()) {
//            throw new RuntimeException("Bank account is inactive for user: " + user.getId());
//        }

        if (account.getVerificationStatus() != ExternalBankAccount.VerificationStatus.VERIFIED) {
            throw new RuntimeException("Bank account not verified for user: " + user.getId());
        }

        return account;
    }

    /**
     * Build Modern Treasury payment order request
     */
    private Map<String, Object> buildPaymentOrderRequest(User user, ExternalBankAccount bankAccount,
                                                         BigDecimal amount, LocalDate effectiveDate,
                                                         PaymentType paymentType, String description) {
        Map<String, Object> request = new HashMap<>();

        // Basic payment order fields
        request.put("type", "ach");
        request.put("subtype", "debit");
        request.put("amount", amount.multiply(new BigDecimal("100")).longValue()); // Convert to cents
        request.put("direction", "debit");
        request.put("currency", "USD");

        // Account information
        request.put("originating_account_id", companyAccountId);
        request.put("receiving_account_id", bankAccount.getMtExternalAccountId());

        // Timing
        request.put("effective_date", effectiveDate.toString());

        // Description and statement descriptor
        request.put("description", description);
        request.put("statement_descriptor", getStatementDescriptor(paymentType));

        // Metadata for tracking and reconciliation
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_id", user.getId().toString());
        metadata.put("user_email", user.getEmail());
        metadata.put("user_name", user.getFirstName() + " " + user.getLastName());
        metadata.put("payment_type", paymentType.toString());
        metadata.put("rent_month", effectiveDate.getYear() + "-" + String.format("%02d", effectiveDate.getMonthValue()));
        metadata.put("created_by", "denari_webhook_system");
        request.put("metadata", metadata);

        // Transaction monitoring (enable for AML compliance)
        request.put("transaction_monitoring_enabled", true);

        // Set priority (normal for rent payments)
        request.put("priority", "normal");

        log.info("💳 Payment order request built:");
        log.info("💳 - Amount (cents): {}", request.get("amount"));
        log.info("💳 - Effective Date: {}", request.get("effective_date"));
        log.info("💳 - Statement Descriptor: {}", request.get("statement_descriptor"));

        return request;
    }

    /**
     * Get statement descriptor based on payment type
     */
    private String getStatementDescriptor(PaymentType paymentType) {
        switch (paymentType) {
            case FIRST_PAYMENT:
                return "DENARI RENT 1/2";
            case SECOND_PAYMENT:
                return "DENARI RENT 2/2";
            case SERVICE_FEE:
                return "DENARI FEE";
            case REFUND:
                return "DENARI REFUND";
            default:
                return "DENARI RENT";
        }
    }

    /**
     * Cancel payment order (if not yet processed)
     */
    public boolean cancelPaymentOrder(String paymentOrderId) {
        try {
            log.info("🚫 Attempting to cancel payment order: {}", paymentOrderId);

            boolean cancelled = modernTreasuryService.cancelPaymentOrder(paymentOrderId);

            if (cancelled) {
                log.info("✅ Payment order cancelled successfully: {}", paymentOrderId);
            } else {
                log.warn("⚠️ Payment order could not be cancelled: {}", paymentOrderId);
            }

            return cancelled;

        } catch (Exception e) {
            log.error("❌ Error cancelling payment order {}: {}", paymentOrderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get payment order status from Modern Treasury
     */
    public PaymentOrderStatus getPaymentOrderStatus(String paymentOrderId) {
        try {
            Map<String, Object> statusResponse = modernTreasuryService.getPaymentOrderStatus(paymentOrderId);

            String status = (String) statusResponse.get("status");
            String failureReason = (String) statusResponse.get("failure_reason");

            return PaymentOrderStatus.builder()
                    .paymentOrderId(paymentOrderId)
                    .status(status)
                    .failureReason(failureReason)
                    .rawResponse(statusResponse)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error getting payment order status for {}: {}", paymentOrderId, e.getMessage());
            throw new RuntimeException("Failed to get payment order status: " + e.getMessage());
        }
    }

    /**
     * Create refund payment order
     */
    public String createRefundPaymentOrder(User user, BigDecimal amount, String reason) {
        try {
            log.info("💰 Creating refund payment order:");
            log.info("💰 - User: {} (ID: {})", user.getEmail(), user.getId());
            log.info("💰 - Amount: ${}", amount);
            log.info("💰 - Reason: {}", reason);

            // Get user's bank account
            ExternalBankAccount bankAccount = getUserBankAccount(user);

            // Build refund request (credit instead of debit)
            Map<String, Object> refundRequest = new HashMap<>();
            refundRequest.put("type", "ach");
            refundRequest.put("subtype", "credit");
            refundRequest.put("amount", amount.multiply(new BigDecimal("100")).longValue());
            refundRequest.put("direction", "credit");
            refundRequest.put("currency", "USD");
            refundRequest.put("originating_account_id", companyAccountId);
            refundRequest.put("receiving_account_id", bankAccount.getMtExternalAccountId());
            refundRequest.put("effective_date", LocalDate.now().toString());
            refundRequest.put("description", "Denari Refund: " + reason);
            refundRequest.put("statement_descriptor", "DENARI REFUND");

            // Add refund metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("user_id", user.getId().toString());
            metadata.put("refund_reason", reason);
            metadata.put("payment_type", "REFUND");
            refundRequest.put("metadata", metadata);

            String paymentOrderId = modernTreasuryService.createPaymentOrder(refundRequest);

            log.info("✅ Refund payment order created: {}", paymentOrderId);
            return paymentOrderId;

        } catch (Exception e) {
            log.error("❌ Failed to create refund for user {}: {}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create refund: " + e.getMessage());
        }
    }

    /**
     * Payment order status response
     */
    @lombok.Builder
    @lombok.Data
    public static class PaymentOrderStatus {
        private String paymentOrderId;
        private String status;
        private String failureReason;
        private Map<String, Object> rawResponse;
    }
}
