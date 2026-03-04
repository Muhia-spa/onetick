package com.onetick.notifications;

import com.onetick.entity.Notification;
import com.onetick.entity.Task;
import com.onetick.entity.enums.NotificationStatus;
import com.onetick.entity.enums.NotificationType;
import com.onetick.repository.NotificationRepository;
import com.onetick.service.EmailService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

@Service
public class NotificationDispatcher {
    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final NotificationQueue notificationQueue;
    private final NotificationQueueProperties properties;
    private final Counter sentCounter;
    private final Counter failedCounter;
    private final Counter retriedCounter;
    private final Counter dlqCounter;
    private final Timer dispatchTimer;

    public NotificationDispatcher(NotificationRepository notificationRepository,
                                  EmailService emailService,
                                  NotificationQueue notificationQueue,
                                  NotificationQueueProperties properties,
                                  MeterRegistry meterRegistry) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.notificationQueue = notificationQueue;
        this.properties = properties;
        this.sentCounter = meterRegistry.counter("notifications.sent");
        this.failedCounter = meterRegistry.counter("notifications.failed");
        this.retriedCounter = meterRegistry.counter("notifications.retried");
        this.dlqCounter = meterRegistry.counter("notifications.dlq");
        this.dispatchTimer = meterRegistry.timer("notifications.dispatch.duration");
    }

    @Transactional
    public void dispatch(Long notificationId) {
        long start = System.nanoTime();
        Optional<Notification> optional = notificationRepository.findWithTaskAndRecipientById(notificationId);
        if (optional.isEmpty()) {
            log.warn("Notification id={} not found", notificationId);
            return;
        }

        Notification notification = optional.get();
        if (notification.getStatus() != NotificationStatus.PENDING) {
            return;
        }

        Task task = notification.getTask();
        String subject = buildSubject(notification.getType(), task);
        String body = buildBody(notification.getType(), task);
        notification.setAttemptCount(notification.getAttemptCount() + 1);
        notification.setLastAttemptAt(Instant.now());

        try {
            emailService.send(notification.getRecipient().getEmail(), subject, body);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setLastError(null);
            sentCounter.increment();
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            notification.setLastError(errorMessage != null && errorMessage.length() > 500
                    ? errorMessage.substring(0, 500)
                    : errorMessage);
            if (notification.getAttemptCount() >= properties.getMaxRetries()) {
                notification.setStatus(NotificationStatus.FAILED);
                notificationQueue.enqueueDlq(notification.getId());
                dlqCounter.increment();
            } else {
                notification.setStatus(NotificationStatus.PENDING);
                long delay = computeBackoff(notification.getAttemptCount());
                notificationQueue.enqueueAt(notification.getId(), Instant.now().toEpochMilli() + delay);
                retriedCounter.increment();
            }
            failedCounter.increment();
            log.warn("Failed to send notification id={} to {}", notification.getId(), notification.getRecipient().getEmail(), ex);
        } finally {
            dispatchTimer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    private long computeBackoff(int attempt) {
        long base = properties.getBackoffBaseMs();
        long delay = base * (1L << Math.max(0, attempt - 1));
        return Math.min(delay, properties.getBackoffMaxMs());
    }

    private String buildSubject(NotificationType type, Task task) {
        return switch (type) {
            case ASSIGNMENT -> "Task assignment: " + task.getTitle();
            case REMINDER -> "Task reminder: " + task.getTitle();
        };
    }

    private String buildBody(NotificationType type, Task task) {
        return switch (type) {
            case ASSIGNMENT -> "You have been assigned a task. Deadline: " + task.getDeadline();
            case REMINDER -> "Reminder: task deadline is approaching. Deadline: " + task.getDeadline();
        };
    }
}
