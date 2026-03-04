package com.onetick.notifications;

public interface NotificationQueue {
    void enqueue(Long notificationId);

    void enqueueAt(Long notificationId, long epochMillis);

    Long dequeue();

    void enqueueDlq(Long notificationId);
}
