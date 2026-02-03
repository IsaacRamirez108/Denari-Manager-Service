package com.denari.manager.models.entity.Webhook;

import com.denari.manager.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "webhook_events")
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "webhook_event_id", unique = true)
    private String eventId;

    @Column(nullable = false)
    private String event;

    @Column(nullable = false)
    private ProcessingStatus processingStatus;

    @Column(columnDefinition = "Text")
    private String payload;

    @Column
    private LocalDateTime receivedAt;

    @Column
    private LocalDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
        if (processingStatus == null) {
            processingStatus = ProcessingStatus.PROCESSING;
        }
    }

    public boolean isCompleted() {
        return ProcessingStatus.COMPLETED.equals(this.processingStatus);
    }

    public boolean isFailed() {
        return ProcessingStatus.FAILED.equals(this.processingStatus);
    }

    public boolean isProcessing() {
        return ProcessingStatus.PROCESSING.equals(this.processingStatus);
    }

    public void markAsCompleted() {
        this.processingStatus = ProcessingStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.processingStatus = ProcessingStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsUnhandled(String reason) {
        this.processingStatus = ProcessingStatus.UNHANDLED;
        this.errorMessage = reason;
        this.processedAt = LocalDateTime.now();
    }
}
