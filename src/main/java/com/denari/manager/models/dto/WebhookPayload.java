package com.denari.manager.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO for incoming Modern Treasury webhook payloads
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayload {
    private String event;
    private JsonNode data;

    // This method extracts the resource ID from data
    public String getResourceId() {
        if (data != null && data.has("id")) {
            return data.get("id").asText();
        }
        return null;
    }
}

