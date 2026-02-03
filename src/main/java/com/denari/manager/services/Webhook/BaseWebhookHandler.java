package com.denari.manager.services.Webhook;

import com.denari.manager.enums.ProcessingStatus;
import com.denari.manager.models.dto.WebhookPayload;
import com.denari.manager.models.entity.Webhook.WebhookEvent;
import com.denari.manager.repositories.WebhookEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public abstract class BaseWebhookHandler {

    protected final WebhookEventRepository webhookEventRepository;
    protected final ObjectMapper objectMapper;

    // ✅ FIX: Explicit constructor instead of @RequiredArgsConstructor
    public BaseWebhookHandler(WebhookEventRepository webhookEventRepository, ObjectMapper objectMapper) {
        this.webhookEventRepository = webhookEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Save webhook event for audit trail and idempotency
     */
    protected WebhookEvent saveWebhookEvent(WebhookPayload payload) {
        try {
            WebhookEvent event = new WebhookEvent();
            event.setEventId(payload.getResourceId());
            event.setEvent(payload.getEvent());
            event.setPayload(objectMapper.writeValueAsString(payload));
            event.setReceivedAt(LocalDateTime.now());
            event.setProcessingStatus(ProcessingStatus.PROCESSING);

            return webhookEventRepository.save(event);
        } catch (Exception e) {
            log.error("❌ Failed to save webhook event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save webhook event: " + e.getMessage());
        }
    }

    /**
     * Mark webhook as successfully processed
     */
    protected void markWebhookAsProcessed(WebhookEvent event) {
        try {
            event.setProcessingStatus(ProcessingStatus.COMPLETED);
            event.setProcessedAt(LocalDateTime.now());
            webhookEventRepository.save(event);

            log.info("✅ Webhook event marked as completed: {}", event.getEventId());
        } catch (Exception e) {
            log.error("❌ Failed to mark webhook as processed: {}", e.getMessage(), e);
        }
    }

    /**
     * Mark webhook as failed with error message
     */
    protected void markWebhookAsFailed(WebhookEvent event, String errorMessage) {
        try {
            event.setProcessingStatus(ProcessingStatus.FAILED);
            event.setErrorMessage(errorMessage);
            event.setProcessedAt(LocalDateTime.now());
            webhookEventRepository.save(event);

            log.error("❌ Webhook event marked as failed: {} - {}", event.getEventId(), errorMessage);
        } catch (Exception e) {
            log.error("❌ Failed to mark webhook as failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Mark webhook as unhandled (unknown event type)
     */
    protected void markWebhookAsUnhandled(WebhookEvent event) {
        try {
            event.setProcessingStatus(ProcessingStatus.UNHANDLED);
            event.setErrorMessage("Unhandled event type: " + event.getEvent());
            event.setProcessedAt(LocalDateTime.now());
            webhookEventRepository.save(event);

            log.warn("⚠️ Webhook event marked as unhandled: {}", event.getEventId());
        } catch (Exception e) {
            log.error("❌ Failed to mark webhook as unhandled: {}", e.getMessage(), e);
        }
    }

    /**
     * Extract string value from JSON node safely
     */
    protected String getStringValue(com.fasterxml.jackson.databind.JsonNode node, String fieldName, String defaultValue) {
        return node.has(fieldName) ? node.get(fieldName).asText(defaultValue) : defaultValue;
    }

    /**
     * Extract long value from JSON node safely
     */
    protected Long getLongValue(com.fasterxml.jackson.databind.JsonNode node, String fieldName, Long defaultValue) {
        return node.has(fieldName) ? node.get(fieldName).asLong(defaultValue) : defaultValue;
    }

    /**
     * Check if webhook event should be processed (not already completed)
     */
    protected boolean shouldProcessEvent(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return true; // Process if no ID (unusual but safe)
        }

        return !webhookEventRepository.isEventAlreadyProcessed(eventId);
    }

    /**
     * Template method - subclasses implement specific event handling
     */
    public abstract void handleEvent(WebhookPayload payload);

    /**
     * Validate that webhook payload has required fields
     */
    protected void validateWebhookPayload(WebhookPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Webhook payload cannot be null");
        }

        if (payload.getEvent() == null || payload.getEvent().isEmpty()) {
            throw new IllegalArgumentException("Webhook event type cannot be null or empty");
        }

        if (payload.getData() == null) {
            throw new IllegalArgumentException("Webhook data cannot be null");
        }
    }

    /**
     * Get handler name for logging
     */
    protected String getHandlerName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Log webhook processing start
     */
    protected void logWebhookStart(String eventType, String resourceId) {
        log.info("🔄 {} processing event: {} (ID: {})", getHandlerName(), eventType, resourceId);
    }

    /**
     * Log webhook processing completion
     */
    protected void logWebhookComplete(String eventType, String resourceId) {
        log.info("✅ {} completed event: {} (ID: {})", getHandlerName(), eventType, resourceId);
    }

    /**
     * Log webhook processing error
     */
    protected void logWebhookError(String eventType, String resourceId, Exception e) {
        log.error("❌ {} failed event: {} (ID: {}) - {}", getHandlerName(), eventType, resourceId, e.getMessage(), e);
    }
}
