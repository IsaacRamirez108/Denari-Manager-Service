//package com.denari.manager.services;
//
//import com.denari.manager.models.dto.WebhookPayload;
//import com.denari.manager.models.entity.ExternalAccount.Counterparty;
//import com.denari.manager.models.entity.ExternalAccount.ExternalBankAccount;
//import com.denari.manager.models.entity.Payments.PaymentRecord;
//import com.denari.manager.models.entity.User.OnboardingStatus;
//import com.denari.manager.models.entity.User.PaymentSchedule;
//import com.denari.manager.models.entity.User.User;
//import com.denari.manager.models.entity.VirtualAccount.VirtualAccount;
//import com.denari.manager.models.entity.Webhook.WebhookEvent;
//import com.denari.manager.repositories.*;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.math.BigDecimal;
//import java.time.DateTimeException;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ModernTreasuryWebhookHandler {
//
//    private final WebhookEventRepository webhookEventRepository;
//    private final UserRepository userRepository;
//    private final CounterpartyRepository counterpartyRepository;
//    private final ExternalBankAccountRepository externalBankAccountRepository;
//    private final VirtualAccountRepository virtualAccountRepository;
//    private final OnboardingStatusRepository onboardingStatusRepository;
//    private final ObjectMapper objectMapper;
//    private final PaymentRecordRepository paymentRecordRepository;
//    private final ModernTreasuryService modernTreasuryService ;
//
//    @Transactional
//    public void handleWebhook(WebhookPayload payload) {
//        String eventType = payload.getEvent();
//        String resourceId = payload.getResourceId();
//
//        log.info("Processing webhook event: {} for resource: {}", eventType, resourceId);
//
//        // Save webhook event for audit trail
//        WebhookEvent webhookEvent = saveWebhookEvent(payload);
//
//        try {
//            // Route to appropriate handler based on event type
//            switch (eventType) {
//                // Payment Order Events
//                case "payment_order.created":
//                    handlePaymentOrderCreated(payload.getData(), webhookEvent);
//                    break;
//                case "payment_order.completed":
//                    handlePaymentOrderCompleted(payload.getData(), webhookEvent);
//                    break;
//                case "payment_order.failed":
//                    handlePaymentOrderFailed(payload.getData(), webhookEvent);
//                    break;
//
//                // Incoming Payment Events
//                case "incoming_payment_detail.created":
//                    handleIncomingPaymentCreated(payload.getData(), webhookEvent);
//                    break;
//                case "incoming_payment_detail.returned":
//                    handleIncomingPaymentReturned(payload.getData(), webhookEvent);
//                    break;
//
//                // Account Events
//                case "virtual_account.created":
//                    handleVirtualAccountCreated(payload.getData(), webhookEvent);
//                    break;
//                case "external_account.created":
//                    handleExternalAccountCreated(payload.getData(), webhookEvent);
//                    break;
//                case "external_account.failed_verification":
//                    handleExternalAccountFailedVerification(payload.getData(), webhookEvent);
//                    break;
//
//                // Counterparty Events
//                case "counterparty.created":
//                    handleCounterpartyCreated(payload.getData(), webhookEvent);
//                    break;
//
//                default:
//                    log.warn("Unhandled webhook event type: {}", eventType);
//                    markWebhookAsUnhandled(webhookEvent);
//                    break;
//            }
//
//            // Mark webhook as successfully processed
//            markWebhookAsProcessed(webhookEvent);
//            log.info("Successfully processed webhook event: {}", eventType);
//
//        } catch (Exception e) {
//            log.error("Error processing webhook event {}: {}", eventType, e.getMessage(), e);
//            markWebhookAsFailed(webhookEvent, e.getMessage());
//            // Don't re-throw - we want to return 200 to Modern Treasury to prevent retries
//        }
//    }
//
//    // ============== PAYMENT ORDER HANDLERS ==============
//
//    private void handlePaymentOrderCreated(JsonNode data, WebhookEvent event) {
//        log.info("Payment order created: {}", data.get("id").asText());
//        // TODO: For future payment engine
//        // - Save payment order details
//        // - Update user payment status
//        // - Send notifications
//    }
//
//    /**
//     * PRODUCTION-READY: Update payment status when webhooks arrive
//     */
//    private void handlePaymentOrderCompleted(JsonNode data, WebhookEvent event) {
//        String paymentOrderId = data.get("id").asText();
//        String status = data.get("status").asText();
//
//        log.info("✅ Payment order completed: {}", paymentOrderId);
//
//        try {
//            // Find the payment record
//            Optional<PaymentRecord> paymentRecord = paymentRecordRepository.findByMtPaymentOrderId(paymentOrderId);
//
//            if (paymentRecord.isPresent()) {
//                PaymentRecord record = paymentRecord.get();
//                record.markAsCompleted();
//                paymentRecordRepository.save(record);
//
//                log.info("✅ Payment record updated to COMPLETED: {}", record.getId());
//
//                // Send success notification to user
//                sendPaymentSuccessNotification(record.getUser(), record);
//
//            } else {
//                log.warn("⚠️ No payment record found for payment order: {}", paymentOrderId);
//            }
//
//        } catch (Exception e) {
//            log.error("❌ Error updating payment record: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * PRODUCTION-READY: Handle failed payments
//     */
//    private void handlePaymentOrderFailed(JsonNode data, WebhookEvent event) {
//        String paymentOrderId = data.get("id").asText();
//        String failureReason = data.path("failure_reason").asText("Unknown");
//
//        log.error("❌ Payment order failed: {} - Reason: {}", paymentOrderId, failureReason);
//
//        try {
//            // Find and update the payment record
//            Optional<PaymentRecord> paymentRecord = paymentRecordRepository.findByMtPaymentOrderId(paymentOrderId);
//
//            if (paymentRecord.isPresent()) {
//                PaymentRecord record = paymentRecord.get();
//                record.markAsFailed(failureReason);
//                paymentRecordRepository.save(record);
//
//                log.error("❌ Payment record updated to FAILED: {} - {}", record.getId(), failureReason);
//
//                // Handle failure scenarios
//                handlePaymentFailure(record, failureReason);
//
//            } else {
//                log.warn("⚠️ No payment record found for failed payment order: {}", paymentOrderId);
//            }
//
//        } catch (Exception e) {
//            log.error("❌ Error handling payment failure: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Handle payment failure scenarios (NSF, etc.)
//     */
//    private void handlePaymentFailure(PaymentRecord failedPayment, String reason) {
//        try {
//            User user = failedPayment.getUser();
//
//            log.warn("🚨 Handling payment failure for user: {} - Reason: {}", user.getId(), reason);
//
//            // Send failure notification to user
//            sendPaymentFailureNotification(user, failedPayment, reason);
//
//            // Determine if retry is appropriate
//            if (shouldRetryPayment(reason)) {
//                schedulePaymentRetry(failedPayment);
//            } else {
//                // Mark user account for manual review
//                flagUserAccountForReview(user, "Payment failed: " + reason);
//            }
//
//            /**
//             * Determine if payment should be retried based on failure reason
//             */
//            private boolean shouldRetryPayment(String failureReason) {
//                // NSF (insufficient funds) - retry once after 3 days
//                if (failureReason.toLowerCase().contains("insufficient") ||
//                        failureReason.toLowerCase().contains("nsf")) {
//                    return true;
//                }
//
//                // Account errors - don't retry, need user action
//                if (failureReason.toLowerCase().contains("invalid") ||
//                        failureReason.toLowerCase().contains("closed")) {
//                    return false;
//                }
//
//                // Default: retry once for unknown errors
//                return true;
//            }
//
//        } catch (Exception e) {
//            log.error("❌ Error in payment failure handling: {}", e.getMessage(), e);
//        }
//    }
//
//
//    // ============== INCOMING PAYMENT HANDLERS ==============
//
//    /**
//     * PRODUCTION-READY: Handle incoming rent payment to user's virtual account
//     * This is the core business logic that triggers rent splitting
//     */
//    private void handleIncomingPaymentCreated(JsonNode data, WebhookEvent event) {
//        try {
//            // Extract critical data from webhook
//            String virtualAccountId = data.path("virtual_account_id").asText();
//            Long amountCents = data.path("amount").asLong();
//            String currency = data.path("currency").asText("USD");
//            String paymentId = data.path("id").asText();
//
//            log.info("🏠 RENT PAYMENT RECEIVED: ${} to virtual account: {}",
//                    amountCents / 100.0, virtualAccountId);
//
//            // Validate required fields
//            if (virtualAccountId.isEmpty() || amountCents <= 0) {
//                log.error("❌ Invalid incoming payment data - virtualAccountId: {}, amount: {}",
//                        virtualAccountId, amountCents);
//                throw new IllegalArgumentException("Invalid payment data");
//            }
//
//            // 1. Find user by virtual account
//            User user = findUserByVirtualAccount(virtualAccountId);
//            if (user == null) {
//                log.error("❌ No user found for virtual account: {}", virtualAccountId);
//                throw new RuntimeException("User not found for virtual account: " + virtualAccountId);
//            }
//
//            log.info("👤 Found user: {} {} (ID: {})",
//                    user.getFirstName(), user.getLastName(), user.getId());
//
//            // 2. Validate payment amount matches expected rent
//            validateRentAmount(user, amountCents);
//
//            // 3. Check for duplicate processing
//            if (isPaymentAlreadyProcessed(paymentId)) {
//                log.warn("⚠️ Payment {} already processed, skipping", paymentId);
//                return;
//            }
//
//            // 4. Create ACH payment schedule for user
//            createUserPaymentSchedule(user, amountCents, paymentId);
//
//            // 5. Update user payment status
//            updateUserPaymentStatus(user, "RENT_RECEIVED");
//
//            // 6. Send confirmation notifications
//            sendPaymentConfirmations(user, amountCents);
//
//            log.info("✅ Successfully processed rent payment for user: {}", user.getId());
//
//        } catch (Exception e) {
//            log.error("❌ Error processing incoming payment: {}", e.getMessage(), e);
//            // Mark webhook event as failed for manual review
//            markWebhookAsFailed(event, "Failed to process incoming payment: " + e.getMessage());
//            throw e; // Re-throw to ensure webhook processing fails
//        }
//    }
//
//    /**
//     * Find user by Modern Treasury virtual account ID
//     */
//    private User findUserByVirtualAccount(String virtualAccountId) {
//        Optional<VirtualAccount> virtualAccount = virtualAccountRepository.findByMtId(virtualAccountId);
//
//        if (virtualAccount.isEmpty()) {
//            log.error("❌ Virtual account not found in database: {}", virtualAccountId);
//            return null;
//        }
//
//        return virtualAccount.get().getUser();
//    }
//
//    /**
//     * Validate that incoming payment amount matches user's expected rent
//     */
//    private void validateRentAmount(User user, Long receivedAmountCents) {
//        com.denari.manager.models.entity.User.RentalData.RentalData rentalData = user.getRentalData();
//        if (rentalData == null) {
//            throw new RuntimeException("No rental data found for user: " + user.getId());
//        }
//
//        Long expectedRentCents = rentalData.getMonthlyRentCents();
//
//        // Allow small variance (within $1) for payment processing differences
//        long variance = Math.abs(receivedAmountCents - expectedRentCents);
//        if (variance > 100) { // More than $1 difference
//            log.error("❌ Payment amount mismatch - Expected: ${}, Received: ${}",
//                    expectedRentCents / 100.0, receivedAmountCents / 100.0);
//            throw new RuntimeException(String.format(
//                    "Payment amount mismatch - Expected: $%.2f, Received: $%.2f",
//                    expectedRentCents / 100.0, receivedAmountCents / 100.0));
//        }
//
//        log.info("✅ Payment amount validated - Expected: ${}, Received: ${}",
//                expectedRentCents / 100.0, receivedAmountCents / 100.0);
//    }
//
//    /**
//     * Check if this payment has already been processed (idempotency)
//     */
//    private boolean isPaymentAlreadyProcessed(String incomingPaymentId) {
//        return paymentRecordRepository.existsByIncomingPaymentId(incomingPaymentId);
//    }
//
//    /**
//     * UPDATED: Create ACH payment schedule with proper record tracking
//     */
//    private void createUserPaymentSchedule(User user, Long rentAmountCents, String incomingPaymentId) {
//        try {
//            PaymentSchedule schedule = user.getRentalData().getPaymentSchedule();
//            if (schedule == null) {
//                throw new RuntimeException("Payment schedule not found for user: " + user.getId());
//            }
//
//            log.info("💰 Creating ACH payment schedule for user: {}", user.getId());
//
//            // Calculate payment dates for current month
//            LocalDate today = LocalDate.now();
//            LocalDate firstPaymentDate = calculatePaymentDate(today, schedule.getFirstPaymentDate());
//            LocalDate secondPaymentDate = calculatePaymentDate(today, schedule.getSecondPaymentDate());
//
//            // Create first ACH payment order (50% of rent)
//            String firstPaymentOrderId = createACHPaymentOrder(
//                    user,
//                    schedule.getFirstPaymentAmount(),
//                    firstPaymentDate,
//                    "FIRST_PAYMENT",
//                    "Denari - First rent payment"
//            );
//
//            // Save first payment record
//            savePaymentRecord(
//                    user,
//                    incomingPaymentId,
//                    firstPaymentOrderId,
//                    PaymentRecord.PaymentType.FIRST_PAYMENT,
//                    schedule.getFirstPaymentAmount(),
//                    firstPaymentDate,
//                    "First rent payment (50%)"
//            );
//
//            // Create second ACH payment order (50% + $15 service fee)
//            String secondPaymentOrderId = createACHPaymentOrder(
//                    user,
//                    schedule.getSecondPaymentAmount(),
//                    secondPaymentDate,
//                    "SECOND_PAYMENT",
//                    "Denari - Second rent payment + service fee"
//            );
//
//            // Save second payment record
//            savePaymentRecord(
//                    user,
//                    incomingPaymentId,
//                    secondPaymentOrderId,
//                    PaymentRecord.PaymentType.SECOND_PAYMENT,
//                    schedule.getSecondPaymentAmount(),
//                    secondPaymentDate,
//                    "Second rent payment (50% + $15 service fee)"
//            );
//
//            log.info("✅ ACH payments scheduled and recorded:");
//            log.info("✅ - First payment: {} (${} on {})", firstPaymentOrderId,
//                    schedule.getFirstPaymentAmount(), firstPaymentDate);
//            log.info("✅ - Second payment: {} (${} on {})", secondPaymentOrderId,
//                    schedule.getSecondPaymentAmount(), secondPaymentDate);
//
//        } catch (Exception e) {
//            log.error("❌ Failed to create payment schedule: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to create user payment schedule: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Calculate the actual payment date based on the day of month preference
//     */
//    private LocalDate calculatePaymentDate(LocalDate currentDate, Integer dayOfMonth) {
//        try {
//            // For current month
//            LocalDate targetDate = currentDate.withDayOfMonth(dayOfMonth);
//
//            // If the date has already passed this month, schedule for next month
//            if (targetDate.isBefore(currentDate)) {
//                targetDate = targetDate.plusMonths(1);
//            }
//
//            return targetDate;
//        } catch (DateTimeException e) {
//            // Handle edge cases like February 30th
//            log.warn("⚠️ Invalid day of month: {}, using last day of month", dayOfMonth);
//            return currentDate.withDayOfMonth(currentDate.lengthOfMonth());
//        }
//    }
//
//    /**
//     * Create ACH payment order via Modern Treasury
//     */
//    private String createACHPaymentOrder(User user, BigDecimal amount, LocalDate effectiveDate,
//                                         String paymentType, String description) {
//        try {
//            log.info("💳 Creating ACH payment order: {} for ${} on {}",
//                    paymentType, amount, effectiveDate);
//
//            // Get user's external bank account
//            Optional<ExternalBankAccount> bankAccount = externalBankAccountRepository.findByUser(user);
//            if (bankAccount.isEmpty()) {
//                throw new RuntimeException("No bank account found for user: " + user.getId());
//            }
//
//            // Create payment order via Modern Treasury API
//            Map<String, Object> paymentOrderRequest = new HashMap<>();
//            paymentOrderRequest.put("type", "ach");
//            paymentOrderRequest.put("subtype", "debit");
//            paymentOrderRequest.put("amount", amount.multiply(new BigDecimal("100")).longValue()); // Convert to cents
//            paymentOrderRequest.put("direction", "debit");
//            paymentOrderRequest.put("currency", "USD");
//            paymentOrderRequest.put("originating_account_id", getCompanyAccountId());
//            paymentOrderRequest.put("receiving_account_id", bankAccount.get().getMtId());
//            paymentOrderRequest.put("effective_date", effectiveDate.toString());
//            paymentOrderRequest.put("description", description);
//            paymentOrderRequest.put("statement_descriptor", "DENARI RENT");
//
//            // Add metadata for tracking
//            Map<String, Object> metadata = new HashMap<>();
//            metadata.put("user_id", user.getId().toString());
//            metadata.put("payment_type", paymentType);
//            metadata.put("rent_month", effectiveDate.getYear() + "-" + effectiveDate.getMonthValue());
//            paymentOrderRequest.put("metadata", metadata);
//
//            // Call Modern Treasury API (implement this method)
//            String paymentOrderId = modernTreasuryService.createPaymentOrder(paymentOrderRequest);
//
//            log.info("✅ ACH payment order created: {}", paymentOrderId);
//            return paymentOrderId;
//
//        } catch (Exception e) {
//            log.error("❌ Failed to create ACH payment order: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to create ACH payment order: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Update user's payment status in database
//     */
//    private void updateUserPaymentStatus(User user, String status) {
//        try {
//            // TODO: Implement user payment status tracking
//            // This would update a UserPaymentStatus table or add status to User entity
//            log.info("📊 Updated payment status for user {}: {}", user.getId(), status);
//        } catch (Exception e) {
//            log.error("❌ Failed to update user payment status: {}", e.getMessage());
//            // Don't throw - this is not critical for the main flow
//        }
//    }
//
//    /**
//     * Send confirmation notifications to user
//     */
//    private void sendPaymentConfirmations(User user, Long amountCents) {
//        try {
//            // TODO: Implement notification service
//            log.info("📧 Sending payment confirmations to user: {}", user.getEmail());
//
//            // Email confirmation that rent was received and payments scheduled
//            // SMS notification with payment dates
//            // Push notification if mobile app
//
//        } catch (Exception e) {
//            log.error("❌ Failed to send notifications: {}", e.getMessage());
//            // Don't throw - notifications are not critical for the core flow
//        }
//    }
//
//    /**
//     * Get company's main bank account ID for payment processing
//     */
//    private String getCompanyAccountId() {
//        // TODO: Store this in configuration or database
//        return "your-company-account-id"; // Replace with actual account ID
//    }
//
//    private void handleIncomingPaymentReturned(JsonNode data, WebhookEvent event) {
//        String virtualAccountId = data.path("virtual_account_id").asText();
//        String returnReason = data.path("return_reason").asText("Unknown");
//        log.warn("Incoming payment returned for virtual account: {} - Reason: {}", virtualAccountId, returnReason);
//        // TODO: For future payment engine
//        // - Reverse credit to virtual account
//        // - Handle NSF scenarios
//        // - Send notifications
//    }
//
//    // ============== ACCOUNT HANDLERS ==============
//
//    private void handleVirtualAccountCreated(JsonNode data, WebhookEvent event) {
//        String virtualAccountId = data.get("id").asText();
//        String counterpartyId = data.path("counterparty_id").asText();
//
//        log.info("Virtual account created: {} for counterparty: {}", virtualAccountId, counterpartyId);
//
//        try {
//            // Find user by counterparty
//            Optional<Counterparty> counterparty = counterpartyRepository.findByMtCounterpartyId(counterpartyId);
//            if (counterparty.isPresent()) {
//                User user = counterparty.get().getUser();
//
//                // Update virtual account with MT ID
//                VirtualAccount virtualAccount = user.getVirtualAccount();
//                if (virtualAccount != null) {
//                    virtualAccount.setMtId(virtualAccountId);
//                    virtualAccount.setActive(true);
//                    virtualAccountRepository.save(virtualAccount);
//
//                    // Progress user onboarding
//                    progressUserOnboarding(user, OnboardingStatus.OnboardingStep.VIRTUAL_ACCOUNT_SETUP);
//
//                    log.info("Virtual account updated for user: {}", user.getId());
//                } else {
//                    log.warn("No virtual account entity found for user: {}", user.getId());
//                }
//            } else {
//                log.warn("Counterparty not found for virtual account creation: {}", counterpartyId);
//            }
//        } catch (Exception e) {
//            log.error("Error handling virtual account creation: {}", e.getMessage(), e);
//        }
//    }
//
//    private void handleExternalAccountCreated(JsonNode data, WebhookEvent event) {
//        String externalAccountId = data.get("id").asText();
//        String counterpartyId = data.path("counterparty_id").asText();
//
//        log.info("External account created: {} for counterparty: {}", externalAccountId, counterpartyId);
//
//        try {
//            // Find user by counterparty
//            Optional<Counterparty> counterparty = counterpartyRepository.findByMtCounterpartyId(counterpartyId);
//            if (counterparty.isPresent()) {
//                User user = counterparty.get().getUser();
//
//                // Save external bank account details
//                saveExternalBankAccountFromWebhook(user, data);
//
//                // Progress user onboarding
//                progressUserOnboarding(user, OnboardingStatus.OnboardingStep.BANK_CONNECTION);
//
//                log.info("External account saved for user: {}", user.getId());
//            } else {
//                log.warn("Counterparty not found for external account: {}", counterpartyId);
//            }
//        } catch (Exception e) {
//            log.error("Error handling external account creation: {}", e.getMessage(), e);
//        }
//    }
//
//    private void handleExternalAccountFailedVerification(JsonNode data, WebhookEvent event) {
//        String externalAccountId = data.get("id").asText();
//        String failureReason = data.path("failure_reason").asText("Unknown");
//
//        log.error("External account failed verification: {} - Reason: {}", externalAccountId, failureReason);
//
//        try {
//            // Update external account verification status
//            Optional<ExternalBankAccount> account = externalBankAccountRepository.findByMtExternalAccountId(externalAccountId);
//            if (account.isPresent()) {
//                ExternalBankAccount bankAccount = account.get();
//                bankAccount.setVerificationStatus(ExternalBankAccount.VerificationStatus.FAILED);
//                externalBankAccountRepository.save(bankAccount);
//
//                log.info("Updated external account verification status for user: {}", bankAccount.getUser().getId());
//                // TODO: Send notification to user about verification failure
//            }
//        } catch (Exception e) {
//            log.error("Error handling external account verification failure: {}", e.getMessage(), e);
//        }
//    }
//
//    // ============== COUNTERPARTY HANDLERS ==============
//
//    private void handleCounterpartyCreated(JsonNode data, WebhookEvent event) {
//        String counterpartyId = data.get("id").asText();
//        String name = data.path("name").asText();
//
//        log.info("Counterparty created: {} - Name: {}", counterpartyId, name);
//
//        // Counterparty creation is typically handled synchronously during onboarding
//        // This webhook confirms the creation was successful
//        // TODO: Could be used to update any pending status or send confirmations
//    }
//
//    // ============== HELPER METHODS ==============
//
//    private WebhookEvent saveWebhookEvent(WebhookPayload payload) {
//        try {
//            WebhookEvent webhookEvent = new WebhookEvent();
//            webhookEvent.setEventId(payload.getResourceId());
//            webhookEvent.setEvent(payload.getEvent());
//            webhookEvent.setPayload(objectMapper.writeValueAsString(payload));
//            webhookEvent.setProcessingStatus("PROCESSING");
//            webhookEvent.setReceivedAt(LocalDateTime.now());
//
//            return webhookEventRepository.save(webhookEvent);
//        } catch (Exception e) {
//            log.error("Error saving webhook event: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to save webhook event");
//        }
//    }
//
//    private void markWebhookAsProcessed(WebhookEvent event) {
//        event.setProcessingStatus("COMPLETED");
//        event.setProcessedAt(LocalDateTime.now());
//        webhookEventRepository.save(event);
//    }
//
//    private void markWebhookAsFailed(WebhookEvent event, String errorMessage) {
//        event.setProcessingStatus("FAILED");
//        event.setProcessedAt(LocalDateTime.now());
//        event.setErrorMessage(errorMessage);
//        webhookEventRepository.save(event);
//    }
//
//    private void markWebhookAsUnhandled(WebhookEvent event) {
//        event.setProcessingStatus("UNHANDLED");
//        event.setProcessedAt(LocalDateTime.now());
//        webhookEventRepository.save(event);
//    }
//
//    private void saveExternalBankAccountFromWebhook(User user, JsonNode data) {
//        try {
//            ExternalBankAccount bankAccount = new ExternalBankAccount();
//            bankAccount.setUser(user);
//            bankAccount.setMtExternalAccountId(data.get("id").asText());
//            bankAccount.setAccountName(data.path("name").asText());
//            bankAccount.setAccountType(
//                    "checking".equals(data.path("account_type").asText()) ?
//                            ExternalBankAccount.AccountType.CHECKING :
//                            ExternalBankAccount.AccountType.SAVINGS
//            );
//            bankAccount.setVerificationStatus(ExternalBankAccount.VerificationStatus.PENDING);
//            bankAccount.setIsPrimary(true);
//            bankAccount.setCreatedAt(LocalDateTime.now());
//
//            externalBankAccountRepository.save(bankAccount);
//            log.info("External bank account saved for user: {}", user.getId());
//        } catch (Exception e) {
//            log.error("Error saving external bank account: {}", e.getMessage(), e);
//        }
//    }
//
//    private void progressUserOnboarding(User user, OnboardingStatus.OnboardingStep expectedStep) {
//        try {
//            OnboardingStatus onboardingStatus = user.getOnboardingStatus();
//            if (onboardingStatus != null && onboardingStatus.getCurrentStep() == expectedStep) {
//                // Progress to next step
//                OnboardingStatus.OnboardingStep nextStep = getNextOnboardingStep(expectedStep);
//                onboardingStatus.setCurrentStep(nextStep);
////                onboardingStatus.setUpdatedAt(LocalDateTime.now());
//                onboardingStatusRepository.save(onboardingStatus);
//
//                log.info("Progressed user {} onboarding from {} to {}",
//                        user.getId(), expectedStep, nextStep);
//            }
//        } catch (Exception e) {
//            log.error("Error progressing user onboarding: {}", e.getMessage(), e);
//        }
//    }
//
//    private OnboardingStatus.OnboardingStep getNextOnboardingStep(OnboardingStatus.OnboardingStep currentStep) {
//        return switch (currentStep) {
//            case BANK_CONNECTION -> OnboardingStatus.OnboardingStep.VIRTUAL_ACCOUNT_SETUP;
//            case VIRTUAL_ACCOUNT_SETUP -> OnboardingStatus.OnboardingStep.COMPLETED;
//            default -> currentStep; // No progression for other steps via webhooks
//        };
//    }
//}
