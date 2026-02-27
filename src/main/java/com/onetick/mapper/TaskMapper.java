package com.onetick.mapper;

import com.onetick.dto.response.TaskResponse;
import com.onetick.entity.Task;

public final class TaskMapper {
    private TaskMapper() {
    }

    public static TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setPriority(task.getPriority());
        response.setStatus(task.getStatus());
        response.setDeadline(task.getDeadline());
        response.setCreatedByUserId(task.getCreatedBy().getId());
        response.setSourceDepartmentId(task.getSourceDepartment().getId());
        response.setTargetDepartmentId(task.getTargetDepartment().getId());
        response.setAssignedToUserId(task.getAssignedTo() == null ? null : task.getAssignedTo().getId());
        response.setProjectId(task.getProject() == null ? null : task.getProject().getId());
        return response;
    }
}
