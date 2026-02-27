package com.onetick.service;

import com.onetick.dto.request.AddCommentRequest;
import com.onetick.dto.request.AssignTaskRequest;
import com.onetick.dto.request.CreateTaskRequest;
import com.onetick.dto.request.UpdateTaskStatusRequest;
import com.onetick.dto.response.TaskCommentResponse;
import com.onetick.dto.response.TaskResponse;

import java.util.List;

public interface TaskService {
    TaskResponse create(CreateTaskRequest request);
    TaskResponse assign(AssignTaskRequest request);
    TaskResponse updateStatus(UpdateTaskStatusRequest request);
    TaskCommentResponse addComment(AddCommentRequest request);
    List<TaskResponse> list();
}
