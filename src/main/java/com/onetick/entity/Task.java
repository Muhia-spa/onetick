package com.onetick.entity;

import com.onetick.entity.enums.TaskPriority;
import com.onetick.entity.enums.TaskStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {
    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.NEW;

    @Column(nullable = false)
    private Instant deadline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_department_id", nullable = false)
    private Department sourceDepartment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_department_id", nullable = false)
    private Department targetDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Department getSourceDepartment() {
        return sourceDepartment;
    }

    public void setSourceDepartment(Department sourceDepartment) {
        this.sourceDepartment = sourceDepartment;
    }

    public Department getTargetDepartment() {
        return targetDepartment;
    }

    public void setTargetDepartment(Department targetDepartment) {
        this.targetDepartment = targetDepartment;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
}
