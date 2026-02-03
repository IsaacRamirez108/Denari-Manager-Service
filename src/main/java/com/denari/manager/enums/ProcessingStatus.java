package com.denari.manager.enums;

import lombok.Getter;

@Getter
public enum ProcessingStatus {
    PROCESSING("Webhook is being processed"),
    COMPLETED("Webhook processed successfully"),
    FAILED("Webhook processing failed"),
    UNHANDLED("Webhook event type not handled"),
    RETRYING("Webhook is being retried after failure");

    private final String description;

    ProcessingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == UNHANDLED;
    }

    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    public boolean requiresAttention() {
        return this == FAILED || this == UNHANDLED;
    }
}

