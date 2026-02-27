package com.onetick.dto.request;

import com.onetick.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateTaskStatusRequest {
    @NotNull
    private Long taskId;

    @NotNull
    private TaskStatus status;

    @NotNull
    private Long changedByUserId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }
}
