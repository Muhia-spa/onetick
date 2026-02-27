package com.onetick.dto.request;

import jakarta.validation.constraints.NotNull;

public class AssignTaskRequest {
    @NotNull
    private Long taskId;

    @NotNull
    private Long assignedToUserId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }
}
