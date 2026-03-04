package com.onetick.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationQueueWorker {
    private static final Logger log = LoggerFactory.getLogger(NotificationQueueWorker.class);

    private final NotificationQueue notificationQueue;
    private final NotificationQueueProperties properties;
    private final NotificationDispatcher dispatcher;

    public NotificationQueueWorker(NotificationQueue notificationQueue,
                                   NotificationQueueProperties properties,
                                   NotificationDispatcher dispatcher) {
        this.notificationQueue = notificationQueue;
        this.properties = properties;
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${app.notifications.queue.poll-ms:2000}")
    public void drainQueue() {
        int processed = 0;
        while (processed < properties.getMaxBatch()) {
            Long id = notificationQueue.dequeue();
            if (id == null) {
                return;
            }
            try {
                dispatcher.dispatch(id);
            } catch (Exception ex) {
                log.warn("Failed to dispatch notification id={}", id, ex);
            }
            processed++;
        }
    }
}
