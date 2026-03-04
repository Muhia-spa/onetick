package com.onetick.notifications;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.PriorityQueue;

@Component
@ConditionalOnProperty(prefix = "app.notifications.queue", name = "backend", havingValue = "local")
public class LocalNotificationQueue implements NotificationQueue {
    private final PriorityQueue<ScheduledItem> queue = new PriorityQueue<>(Comparator.comparingLong(a -> a.dueAt));

    @Override
    public synchronized void enqueue(Long notificationId) {
        enqueueAt(notificationId, System.currentTimeMillis());
    }

    @Override
    public synchronized Long dequeue() {
        ScheduledItem head = queue.peek();
        if (head == null || head.dueAt > System.currentTimeMillis()) {
            return null;
        }
        return queue.poll().id;
    }

    @Override
    public synchronized void enqueueAt(Long notificationId, long epochMillis) {
        queue.offer(new ScheduledItem(notificationId, epochMillis));
    }

    @Override
    public synchronized void enqueueDlq(Long notificationId) {
        queue.offer(new ScheduledItem(notificationId, System.currentTimeMillis()));
    }

    private static final class ScheduledItem {
        private final Long id;
        private final long dueAt;

        private ScheduledItem(Long id, long dueAt) {
            this.id = id;
            this.dueAt = dueAt;
        }
    }
}
