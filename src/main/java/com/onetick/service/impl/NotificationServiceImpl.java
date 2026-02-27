package com.onetick.service.impl;

import com.onetick.entity.Notification;
import com.onetick.entity.Task;
import com.onetick.entity.User;
import com.onetick.entity.enums.NotificationStatus;
import com.onetick.entity.enums.NotificationType;
import com.onetick.repository.NotificationRepository;
import com.onetick.service.EmailService;
import com.onetick.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Override
    @Async("notificationExecutor")
    @Transactional
    public void notifyUser(Task task, User recipient, NotificationType type) {
        Notification notification = new Notification();
        notification.setTask(task);
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setStatus(NotificationStatus.PENDING);

        Notification saved = notificationRepository.save(notification);
        String subject = buildSubject(type, task);
        String body = buildBody(type, task);

        try {
            emailService.send(recipient.getEmail(), subject, body);
            saved.setStatus(NotificationStatus.SENT);
            saved.setSentAt(Instant.now());
            notificationRepository.save(saved);
        } catch (Exception ex) {
            saved.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(saved);
            log.warn("Failed to send notification id={} to {}", saved.getId(), recipient.getEmail(), ex);
        }
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
