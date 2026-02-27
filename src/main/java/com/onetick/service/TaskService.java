package com.onetick.service;

import com.onetick.dto.request.AddCommentRequest;
import com.onetick.dto.request.AssignTaskRequest;
import com.onetick.dto.request.CreateTaskRequest;
import com.onetick.dto.request.UpdateTaskStatusRequest;
import com.onetick.dto.response.PaginatedResponse;
import com.onetick.dto.response.TaskCommentResponse;
import com.onetick.dto.response.TaskResponse;
import com.onetick.entity.enums.TaskStatus;

public interface TaskService {
    TaskResponse create(CreateTaskRequest request);
    TaskResponse assign(AssignTaskRequest request);
    TaskResponse updateStatus(UpdateTaskStatusRequest request);
    TaskCommentResponse addComment(AddCommentRequest request);
    PaginatedResponse<TaskResponse> list(int page, int size, TaskStatus status, Long assignedToUserId, Long projectId);
}
