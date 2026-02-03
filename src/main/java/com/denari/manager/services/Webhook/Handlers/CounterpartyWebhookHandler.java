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
public class CounterpartyWebhookHandler extends BaseWebhookHandler {

    // ✅ FIX: Use explicit constructor instead of @RequiredArgsConstructor
    public CounterpartyWebhookHandler(WebhookEventRepository webhookEventRepository,
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
                case "counterparty.created":
                    handleCounterpartyCreated(payload.getData());
                    break;
                case "counterparty.updated":
                    handleCounterpartyUpdated(payload.getData());
                    break;
                case "counterparty.deleted":
                    handleCounterpartyDeleted(payload.getData());
                    break;
                default:
                    log.warn("⚠️ Unhandled counterparty event: {}", eventType);
                    return;
            }

            logWebhookComplete(eventType, resourceId);

        } catch (Exception e) {
            logWebhookError(eventType, resourceId, e);
            throw e;
        }
    }

    private void handleCounterpartyCreated(com.fasterxml.jackson.databind.JsonNode data) {
        String counterpartyId = getStringValue(data, "id", "");
        String name = getStringValue(data, "name", "");
        String email = getStringValue(data, "email", "");

        log.info("👤 Counterparty created: {} - Name: {}, Email: {}", counterpartyId, name, email);

        // TODO: Implement counterparty creation logic
        // - Update Counterparty entity in database
        // - Link to user if not already linked
        // - Update onboarding status
    }

    private void handleCounterpartyUpdated(com.fasterxml.jackson.databind.JsonNode data) {
        String counterpartyId = getStringValue(data, "id", "");
        String name = getStringValue(data, "name", "");

        log.info("🔄 Counterparty updated: {} - Name: {}", counterpartyId, name);

        // TODO: Implement counterparty update logic
        // - Update Counterparty entity in database
        // - Sync any changes with user profile
    }

    private void handleCounterpartyDeleted(com.fasterxml.jackson.databind.JsonNode data) {
        String counterpartyId = getStringValue(data, "id", "");

        log.warn("🗑️ Counterparty deleted: {}", counterpartyId);

        // TODO: Implement counterparty deletion logic
        // - Mark counterparty as deleted in database
        // - Handle any cleanup needed
        // - Alert if this affects active users
    }
}
