package com.onetick.controller;

import com.onetick.dto.request.AddCommentRequest;
import com.onetick.dto.request.AssignTaskRequest;
import com.onetick.dto.request.CreateTaskRequest;
import com.onetick.dto.request.UpdateTaskStatusRequest;
import com.onetick.dto.response.PaginatedResponse;
import com.onetick.dto.response.TaskCommentResponse;
import com.onetick.dto.response.TaskResponse;
import com.onetick.entity.enums.TaskStatus;
import com.onetick.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD')")
    public ResponseEntity<TaskResponse> assign(@Valid @RequestBody AssignTaskRequest request) {
        return ResponseEntity.ok(taskService.assign(request));
    }

    @PostMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<TaskResponse> updateStatus(@Valid @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(taskService.updateStatus(request));
    }

    @PostMapping("/comments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<TaskCommentResponse> addComment(@Valid @RequestBody AddCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.addComment(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<PaginatedResponse<TaskResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long assignedToUserId,
            @RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(taskService.list(page, size, status, assignedToUserId, projectId));
    }
}
