package com.onetick.scheduler;

import com.onetick.entity.Task;
import com.onetick.entity.enums.NotificationType;
import com.onetick.entity.enums.TaskStatus;
import com.onetick.repository.TaskRepository;
import com.onetick.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;

@Component
public class TaskReminderScheduler {
    private static final Logger log = LoggerFactory.getLogger(TaskReminderScheduler.class);

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    @Value("${app.reminders.hours-before-deadline:24}")
    private long reminderHours;

    public TaskReminderScheduler(TaskRepository taskRepository, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${app.reminders.cron:0 0 * * * *}")
    public void sendDeadlineReminders() {
        Instant now = Instant.now();
        Instant threshold = now.plus(reminderHours, ChronoUnit.HOURS);
        List<Task> tasks = taskRepository.findAllByStatusInAndDeadlineBetween(
                EnumSet.of(TaskStatus.NEW, TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED),
                now,
                threshold
        );

        for (Task task : tasks) {
            if (task.getAssignedTo() != null) {
                notificationService.notifyUser(task, task.getAssignedTo(), NotificationType.REMINDER);
            }
        }

        log.info("Reminder job processed {} tasks", tasks.size());
    }
}
