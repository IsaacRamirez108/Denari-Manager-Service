package com.denari.manager.services.Webhook.Handlers;

import com.denari.manager.models.dto.WebhookPayload;
import com.denari.manager.repositories.WebhookEventRepository;
import com.denari.manager.services.Webhook.BaseWebhookHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class AccountWebhookHandler extends BaseWebhookHandler {

    // ✅ FIX: Use explicit constructor instead of @RequiredArgsConstructor
    public AccountWebhookHandler(WebhookEventRepository webhookEventRepository,
                                 ObjectMapper objectMapper) {
        super(webhookEventRepository, objectMapper);
    }

    @Override
    public void handleEvent(WebhookPayload payload) {
        validateWebhookPayload(payload);

        String eventType = payload.getEvent();
        String resourceId = payload.getResourceId();

        logWebhookStart(eventType, resourceId);

        try {
            switch (eventType) {
                case "virtual_account.created":
                    handleVirtualAccountCreated(payload.getData());
                    break;
                case "external_account.created":
                    handleExternalAccountCreated(payload.getData());
                    break;
                case "external_account.failed_verification":
                    handleExternalAccountFailedVerification(payload.getData());
                    break;
                case "external_account.updated":
                    handleExternalAccountUpdated(payload.getData());
                    break;
                default:
                    log.warn("⚠️ Unhandled account event: {}", eventType);
                    return;
            }

            logWebhookComplete(eventType, resourceId);

        } catch (Exception e) {
            logWebhookError(eventType, resourceId, e);
            throw e;
        }
    }

    private void handleVirtualAccountCreated(com.fasterxml.jackson.databind.JsonNode data) {
        String virtualAccountId = getStringValue(data, "id", "");
        String counterpartyId = getStringValue(data, "counterparty_id", "");

        log.info("🏦 Virtual account created: {} for counterparty: {}", virtualAccountId, counterpartyId);

        // TODO: Implement virtual account creation logic
        // - Update VirtualAccount entity in database
        // - Link to user if not already linked
        // - Update onboarding status if needed
    }

    private void handleExternalAccountCreated(com.fasterxml.jackson.databind.JsonNode data) {
        String externalAccountId = getStringValue(data, "id", "");
        String counterpartyId = getStringValue(data, "counterparty_id", "");
        String verificationStatus = getStringValue(data, "verification_status", "");

        log.info("🏦 External account created: {} for counterparty: {} (status: {})",
                externalAccountId, counterpartyId, verificationStatus);

        // TODO: Implement external account creation logic
        // - Update ExternalBankAccount entity in database
        // - Update verification status
        // - Progress onboarding if verification successful
    }

    private void handleExternalAccountFailedVerification(com.fasterxml.jackson.databind.JsonNode data) {
        String externalAccountId = getStringValue(data, "id", "");
        String failureReason = getStringValue(data, "failure_reason", "Unknown");

        log.error("❌ External account verification failed: {} - Reason: {}",
                externalAccountId, failureReason);

        // TODO: Implement verification failure logic
        // - Mark account as failed verification
        // - Notify user to retry bank connection
        // - Halt onboarding process
    }

    private void handleExternalAccountUpdated(com.fasterxml.jackson.databind.JsonNode data) {
        String externalAccountId = getStringValue(data, "id", "");
        String verificationStatus = getStringValue(data, "verification_status", "");

        log.info("🔄 External account updated: {} - Status: {}", externalAccountId, verificationStatus);

        // TODO: Implement account update logic
        // - Update verification status in database
        // - Progress onboarding if now verified
    }
}
