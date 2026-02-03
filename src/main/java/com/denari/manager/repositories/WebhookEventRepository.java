package com.denari.manager.repositories;

import com.denari.manager.enums.ProcessingStatus;
import com.denari.manager.models.entity.Webhook.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    // ✅ SIMPLE: Use method-based query - Spring generates this automatically
    boolean existsByEventIdAndProcessingStatus(String eventId, ProcessingStatus status);

    // ✅ SIMPLE: Method to check if event is already processed
    default boolean isEventAlreadyProcessed(String eventId) {
        return existsByEventIdAndProcessingStatus(eventId, ProcessingStatus.COMPLETED);
    }

    // ✅ SIMPLE: Method-based queries (Spring generates these)
    long countByProcessingStatus(ProcessingStatus status);

    List<WebhookEvent> findByProcessingStatus(ProcessingStatus status);

    List<WebhookEvent> findByEventContaining(String eventType);

    WebhookEvent findByEventId(String eventId);

    // ✅ SIMPLE: Find events that need attention
    List<WebhookEvent> findByProcessingStatusIn(List<ProcessingStatus> statuses);

    // ✅ Use this method to find events needing attention
    default List<WebhookEvent> findEventsNeedingAttention() {
        return findByProcessingStatusIn(List.of(ProcessingStatus.FAILED, ProcessingStatus.UNHANDLED));
    }

    // ✅ SIMPLE: Find recent events
    List<WebhookEvent> findByReceivedAtAfterOrderByReceivedAtDesc(LocalDateTime since);

    // ✅ Find stuck processing events
    List<WebhookEvent> findByProcessingStatusAndReceivedAtBefore(ProcessingStatus status, LocalDateTime cutoff);

    // ✅ Only keep the complex queries that actually need JPQL
    @Query("SELECT w.processingStatus, COUNT(w) FROM WebhookEvent w GROUP BY w.processingStatus")
    List<Object[]> getProcessingStatistics();
}
