package com.onetick.service.impl;

import com.onetick.dto.request.AddCommentRequest;
import com.onetick.dto.request.AssignTaskRequest;
import com.onetick.dto.request.CreateTaskRequest;
import com.onetick.dto.request.UpdateTaskStatusRequest;
import com.onetick.dto.response.TaskCommentResponse;
import com.onetick.dto.response.TaskResponse;
import com.onetick.entity.Department;
import com.onetick.entity.Task;
import com.onetick.entity.TaskComment;
import com.onetick.entity.TaskStatusHistory;
import com.onetick.entity.User;
import com.onetick.entity.enums.NotificationType;
import com.onetick.entity.enums.TaskStatus;
import com.onetick.exception.BadRequestException;
import com.onetick.exception.NotFoundException;
import com.onetick.mapper.TaskCommentMapper;
import com.onetick.mapper.TaskMapper;
import com.onetick.repository.DepartmentRepository;
import com.onetick.repository.TaskCommentRepository;
import com.onetick.repository.TaskRepository;
import com.onetick.repository.TaskStatusHistoryRepository;
import com.onetick.repository.UserRepository;
import com.onetick.service.NotificationService;
import com.onetick.service.TaskService;
import com.onetick.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final TaskStatusHistoryRepository statusHistoryRepository;
    private final TaskCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;

    public TaskServiceImpl(TaskRepository taskRepository,
                           TaskStatusHistoryRepository statusHistoryRepository,
                           TaskCommentRepository commentRepository,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        if (request.getDeadline().isBefore(java.time.Instant.now())) {
            throw new BadRequestException("Deadline must be in the future");
        }
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(request.getCreatedByUserId())) {
            throw new BadRequestException("createdByUserId must match the authenticated user");
        }
        User createdBy = userRepository.findById(request.getCreatedByUserId())
                .orElseThrow(() -> new NotFoundException("Creator not found"));
        Department source = departmentRepository.findById(request.getSourceDepartmentId())
                .orElseThrow(() -> new NotFoundException("Source department not found"));
        Department target = departmentRepository.findById(request.getTargetDepartmentId())
                .orElseThrow(() -> new NotFoundException("Target department not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());
        task.setCreatedBy(createdBy);
        task.setSourceDepartment(source);
        task.setTargetDepartment(target);

        Task saved = taskRepository.save(task);
        log.info("Created task id={}", saved.getId());
        return TaskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse assign(AssignTaskRequest request) {
        enforceAssignerRole();
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new NotFoundException("Task not found"));
        User assignee = userRepository.findById(request.getAssignedToUserId())
                .orElseThrow(() -> new NotFoundException("Assignee not found"));
        if (!assignee.isActive()) {
            throw new BadRequestException("Assignee is inactive");
        }

        task.setAssignedTo(assignee);
        Task saved = taskRepository.save(task);
        notificationService.notifyUser(saved, assignee, NotificationType.ASSIGNMENT);
        log.info("Assigned task id={} to user id={}", saved.getId(), assignee.getId());
        return TaskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(UpdateTaskStatusRequest request) {
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(request.getChangedByUserId())) {
            throw new BadRequestException("changedByUserId must match the authenticated user");
        }
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new NotFoundException("Task not found"));
        User changedBy = userRepository.findById(request.getChangedByUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (SecurityUtils.hasRole("STAFF")) {
            boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(changedBy.getId());
            boolean isCreator = task.getCreatedBy().getId().equals(changedBy.getId());
            if (!isAssignee && !isCreator) {
                throw new BadRequestException("Staff can only update tasks assigned to them or created by them");
            }
        }

        TaskStatus current = task.getStatus();
        TaskStatus next = request.getStatus();
        if (!isValidTransition(current, next)) {
            throw new BadRequestException("Invalid status transition: " + current + " -> " + next);
        }

        task.setStatus(next);
        Task saved = taskRepository.save(task);

        TaskStatusHistory history = new TaskStatusHistory();
        history.setTask(saved);
        history.setOldStatus(current);
        history.setNewStatus(next);
        history.setChangedBy(changedBy);
        statusHistoryRepository.save(history);

        log.info("Updated task status id={} {}->{}", saved.getId(), current, next);
        return TaskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskCommentResponse addComment(AddCommentRequest request) {
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(request.getCreatedByUserId())) {
            throw new BadRequestException("createdByUserId must match the authenticated user");
        }
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new NotFoundException("Task not found"));
        User author = userRepository.findById(request.getCreatedByUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setCreatedBy(author);
        comment.setComment(request.getComment());
        TaskComment saved = commentRepository.save(comment);
        log.info("Added comment id={} on task id={}", saved.getId(), task.getId());
        return TaskCommentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> list() {
        return taskRepository.findAll().stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    private void enforceAssignerRole() {
        if (!(SecurityUtils.hasRole("ADMIN") || SecurityUtils.hasRole("MANAGER") || SecurityUtils.hasRole("TEAM_LEAD"))) {
            throw new BadRequestException("Not authorized to assign tasks");
        }
    }

    private User getCurrentUser() {
        String email = SecurityUtils.currentUsername();
        if (email == null) {
            throw new BadRequestException("Unauthenticated request");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }

    private boolean isValidTransition(TaskStatus current, TaskStatus next) {
        if (current == next) {
            return true;
        }
        return switch (current) {
            case NEW -> EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED, TaskStatus.CANCELED).contains(next);
            case IN_PROGRESS -> EnumSet.of(TaskStatus.BLOCKED, TaskStatus.DONE, TaskStatus.CANCELED).contains(next);
            case BLOCKED -> EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELED).contains(next);
            case DONE, CANCELED -> false;
        };
    }
}
