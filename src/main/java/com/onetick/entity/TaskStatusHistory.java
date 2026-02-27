package com.onetick.entity;

import com.onetick.entity.enums.TaskStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "task_status_history")
public class TaskStatusHistory extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedBy;

    @Column(nullable = false, name = "changed_at")
    private Instant changedAt = Instant.now();

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public TaskStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(TaskStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public TaskStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TaskStatus newStatus) {
        this.newStatus = newStatus;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }
}
