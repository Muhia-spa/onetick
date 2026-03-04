package com.onetick.notifications;

import com.onetick.entity.enums.NotificationStatus;
import com.onetick.repository.NotificationRepository;
import com.onetick.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
@ActiveProfiles("test")
@AutoConfigureTestDatabase
class NotificationDispatcherTest {

    @Autowired
    private NotificationDispatcher dispatcher;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationQueueProperties properties;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationQueue notificationQueue;

    @BeforeEach
    void setup() {
        properties.setBackoffBaseMs(10);
        properties.setBackoffMaxMs(50);
    }

    @Test
    void dispatchFailureShouldRetryBeforeDlq() {
        properties.setMaxRetries(3);
        Long notificationId = seedNotification();

        doThrow(new RuntimeException("smtp down")).when(emailService)
                .send(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString());

        dispatcher.dispatch(notificationId);

        var saved = notificationRepository.findById(notificationId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(saved.getAttemptCount()).isEqualTo(1);
        assertThat(saved.getLastError()).contains("smtp down");

        verify(notificationQueue).enqueueAt(anyLong(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void dispatchFailureShouldSendToDlqWhenRetriesExceeded() {
        properties.setMaxRetries(1);
        Long notificationId = seedNotification();

        doThrow(new RuntimeException("smtp down")).when(emailService)
                .send(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString());

        dispatcher.dispatch(notificationId);

        var saved = notificationRepository.findById(notificationId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(saved.getAttemptCount()).isEqualTo(1);
        assertThat(saved.getLastError()).contains("smtp down");

        verify(notificationQueue).enqueueDlq(notificationId);
    }

    private Long seedNotification() {
        Long workspaceId = jdbcTemplate.queryForObject(
                "SELECT id FROM workspaces WHERE code = 'DEFAULT'",
                Long.class
        );
        Long adminId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = 'admin@onetick.local'",
                Long.class
        );

        String suffix = String.valueOf(System.nanoTime());
        String deptCode = "NTF_" + suffix;
        String deptName = "Notify Dept " + suffix;
        jdbcTemplate.update("""
                INSERT INTO departments(name, code, active, workspace_id, created_at, updated_at, version)
                VALUES (?, ?, true, ?, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0)
                """, deptName, deptCode, workspaceId);

        Long departmentId = jdbcTemplate.queryForObject(
                "SELECT id FROM departments WHERE code = ?",
                Long.class,
                deptCode
        );

        String taskTitle = "Notify Task " + System.nanoTime();
        jdbcTemplate.update("""
                INSERT INTO tasks(title, description, priority, status, deadline, created_by_user_id,
                                  source_department_id, target_department_id, assigned_to_user_id, project_id,
                                  created_at, updated_at, version)
                VALUES (?, ?, 'MEDIUM', 'NEW',
                        DATEADD('DAY', 2, CURRENT_TIMESTAMP()), ?, ?, ?, ?, null,
                        CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0)
                """, taskTitle, "Notify Desc", adminId, departmentId, departmentId, adminId);

        Long taskId = jdbcTemplate.queryForObject(
                "SELECT id FROM tasks WHERE title = ?",
                Long.class,
                taskTitle
        );

        jdbcTemplate.update("""
                INSERT INTO notifications(task_id, type, recipient_user_id, status, attempt_count, created_at, updated_at, version)
                VALUES (?, 'ASSIGNMENT', ?, 'PENDING', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0)
                """, taskId, adminId);

        return jdbcTemplate.queryForObject(
                "SELECT id FROM notifications WHERE task_id = ? AND recipient_user_id = ?",
                Long.class,
                taskId,
                adminId
        );
    }
}
