package com.onetick.service.impl;

import com.onetick.dto.request.AddCommentRequest;
import com.onetick.dto.request.AssignTaskRequest;
import com.onetick.dto.request.CreateTaskRequest;
import com.onetick.dto.request.UpdateTaskStatusRequest;
import com.onetick.dto.response.PaginatedResponse;
import com.onetick.dto.response.TaskCommentResponse;
import com.onetick.dto.response.TaskResponse;
import com.onetick.entity.Department;
import com.onetick.entity.Project;
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
import com.onetick.repository.ProjectRepository;
import com.onetick.repository.TaskCommentRepository;
import com.onetick.repository.TaskRepository;
import com.onetick.repository.TaskStatusHistoryRepository;
import com.onetick.repository.UserRepository;
import com.onetick.service.NotificationService;
import com.onetick.service.TaskService;
import com.onetick.service.AuditLogService;
import com.onetick.service.GovernanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final TaskStatusHistoryRepository statusHistoryRepository;
    private final TaskCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;
    private final GovernanceService governanceService;
    private final AuditLogService auditLogService;

    public TaskServiceImpl(TaskRepository taskRepository,
                           TaskStatusHistoryRepository statusHistoryRepository,
                           TaskCommentRepository commentRepository,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           ProjectRepository projectRepository,
                           NotificationService notificationService,
                           GovernanceService governanceService,
                           AuditLogService auditLogService) {
        this.taskRepository = taskRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.projectRepository = projectRepository;
        this.notificationService = notificationService;
        this.governanceService = governanceService;
        this.auditLogService = auditLogService;
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
        governanceService.assertWorkspaceAccess(source.getWorkspace().getId());
        if (!source.getWorkspace().getId().equals(target.getWorkspace().getId())) {
            throw new BadRequestException("Source and target departments must belong to the same workspace");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());
        task.setCreatedBy(createdBy);
        task.setSourceDepartment(source);
        task.setTargetDepartment(target);
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new NotFoundException("Project not found"));
            if (!project.getWorkspace().getId().equals(source.getWorkspace().getId())) {
                throw new BadRequestException("Project workspace must match task departments workspace");
            }
            task.setProject(project);
        }

        Task saved = taskRepository.save(task);
        log.info("Created task id={}", saved.getId());
        auditLogService.log("TASK_CREATE", "Task", saved.getId(), source.getWorkspace().getId(),
                Map.of("priority", saved.getPriority().name(), "status", saved.getStatus().name()));
        return TaskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse assign(AssignTaskRequest request) {
        enforceAssignerRole();
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new NotFoundException("Task not found"));
        governanceService.assertWorkspaceAccess(task.getSourceDepartment().getWorkspace().getId());
        User assignee = userRepository.findById(request.getAssignedToUserId())
                .orElseThrow(() -> new NotFoundException("Assignee not found"));
        if (!assignee.isActive()) {
            throw new BadRequestException("Assignee is inactive");
        }

        task.setAssignedTo(assignee);
        Task saved = taskRepository.save(task);
        notificationService.notifyUser(saved, assignee, NotificationType.ASSIGNMENT);
        log.info("Assigned task id={} to user id={}", saved.getId(), assignee.getId());
        auditLogService.log("TASK_ASSIGN", "Task", saved.getId(), saved.getSourceDepartment().getWorkspace().getId(),
                Map.of("assignedToUserId", assignee.getId()));
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
        governanceService.assertWorkspaceAccess(task.getSourceDepartment().getWorkspace().getId());
        User changedBy = userRepository.findById(request.getChangedByUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (com.onetick.util.SecurityUtils.hasRole("STAFF")) {
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
        auditLogService.log("TASK_STATUS_UPDATE", "Task", saved.getId(), saved.getSourceDepartment().getWorkspace().getId(),
                Map.of("oldStatus", current.name(), "newStatus", next.name()));
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
        governanceService.assertWorkspaceAccess(task.getSourceDepartment().getWorkspace().getId());
        User author = userRepository.findById(request.getCreatedByUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setCreatedBy(author);
        comment.setComment(request.getComment());
        TaskComment saved = commentRepository.save(comment);
        log.info("Added comment id={} on task id={}", saved.getId(), task.getId());
        auditLogService.log("TASK_COMMENT_ADD", "TaskComment", saved.getId(), task.getSourceDepartment().getWorkspace().getId(),
                Map.of("taskId", task.getId()));
        return TaskCommentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TaskResponse> list(int page, int size, TaskStatus status, Long assignedToUserId, Long projectId) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Task> spec = (root, query, cb) -> cb.conjunction();

        if (!com.onetick.util.SecurityUtils.hasRole("ADMIN")) {
            Long workspaceId = governanceService.currentPrimaryWorkspaceIdOrThrow();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sourceDepartment").get("workspace").get("id"), workspaceId));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (assignedToUserId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignedTo").get("id"), assignedToUserId));
        }
        if (projectId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("project").get("id"), projectId));
        }

        var result = taskRepository.findAll(spec, pageable);
        var items = result.getContent().stream()
                .map(TaskMapper::toResponse)
                .toList();
        return PaginatedResponse.of(items, page, size, result.getTotalElements(), result.getTotalPages());
    }

    private void enforceAssignerRole() {
        if (!(com.onetick.util.SecurityUtils.hasRole("ADMIN")
                || com.onetick.util.SecurityUtils.hasRole("MANAGER")
                || com.onetick.util.SecurityUtils.hasRole("TEAM_LEAD"))) {
            throw new BadRequestException("Not authorized to assign tasks");
        }
    }

    private User getCurrentUser() {
        return governanceService.currentUserOrThrow();
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
