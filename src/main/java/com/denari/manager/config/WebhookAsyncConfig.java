package com.denari.manager.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
public class WebhookAsyncConfig {

    /**
     * Dedicated thread pool for webhook processing
     * Configured for high throughput and reliability
     */
    @Bean(name = "webhookExecutor")
    public Executor webhookExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size - always keep these threads alive
        executor.setCorePoolSize(2);

        // Maximum pool size - can scale up to this during high load
        executor.setMaxPoolSize(10);

        // Queue capacity - buffer for pending webhook processing
        executor.setQueueCapacity(100);

        // Thread naming for easier debugging
        executor.setThreadNamePrefix("webhook-async-");

        // Keep alive time for idle threads above core size
        executor.setKeepAliveSeconds(60);

        // What to do when queue is full
        executor.setRejectedExecutionHandler(new WebhookRejectedExecutionHandler());

        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        log.info("Webhook async executor configured with core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * Custom rejection handler for webhook processing
     * Logs critical failures when webhook queue is full
     */
    private static class WebhookRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.error("CRITICAL: Webhook processing queue is full! " +
                            "Active threads: {}, Queue size: {}, Task rejected: {}",
                    executor.getActiveCount(),
                    executor.getQueue().size(),
                    r.toString());

            // In production, you might want to:
            // - Send alerts to monitoring system
            // - Store webhook in dead letter queue
            // - Implement circuit breaker pattern

            throw new RuntimeException("Webhook processing queue is full - task rejected");
        }
    }
}
