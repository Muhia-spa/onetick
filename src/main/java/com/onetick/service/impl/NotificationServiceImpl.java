package com.onetick.service.impl;

import com.onetick.entity.Notification;
import com.onetick.entity.Task;
import com.onetick.entity.User;
import com.onetick.entity.enums.NotificationStatus;
import com.onetick.entity.enums.NotificationType;
import com.onetick.notifications.NotificationQueue;
import com.onetick.repository.NotificationRepository;
import com.onetick.service.NotificationService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationQueue notificationQueue;
    private final Counter enqueuedCounter;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   NotificationQueue notificationQueue,
                                   MeterRegistry meterRegistry) {
        this.notificationRepository = notificationRepository;
        this.notificationQueue = notificationQueue;
        this.enqueuedCounter = meterRegistry.counter("notifications.enqueued");
    }

    @Override
    @Transactional
    public void notifyUser(Task task, User recipient, NotificationType type) {
        Notification notification = new Notification();
        notification.setTask(task);
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setStatus(NotificationStatus.PENDING);

        try {
            Notification saved = notificationRepository.save(notification);
            notificationQueue.enqueue(saved.getId());
            enqueuedCounter.increment();
        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            log.warn("Failed to enqueue notification to {}", recipient.getEmail(), ex);
        }
    }
}
