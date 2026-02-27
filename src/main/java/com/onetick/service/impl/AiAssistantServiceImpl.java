package com.onetick.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onetick.dto.request.CreateAiStatusChangeProposalRequest;
import com.onetick.dto.request.ReviewAiActionProposalRequest;
import com.onetick.dto.request.UpdateTaskStatusRequest;
import com.onetick.dto.response.AiActionProposalResponse;
import com.onetick.dto.response.PaginatedResponse;
import com.onetick.entity.AiActionProposal;
import com.onetick.entity.Task;
import com.onetick.entity.User;
import com.onetick.entity.enums.AiActionStatus;
import com.onetick.entity.enums.AiActionType;
import com.onetick.entity.enums.TaskStatus;
import com.onetick.exception.BadRequestException;
import com.onetick.exception.NotFoundException;
import com.onetick.mapper.AiActionProposalMapper;
import com.onetick.repository.AiActionProposalRepository;
import com.onetick.repository.TaskRepository;
import com.onetick.service.AiAssistantService;
import com.onetick.service.AuditLogService;
import com.onetick.service.GovernanceService;
import com.onetick.service.TaskService;
import com.onetick.util.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {
    private final AiActionProposalRepository proposalRepository;
    private final TaskRepository taskRepository;
    private final GovernanceService governanceService;
    private final TaskService taskService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public AiAssistantServiceImpl(AiActionProposalRepository proposalRepository,
                                  TaskRepository taskRepository,
                                  GovernanceService governanceService,
                                  TaskService taskService,
                                  AuditLogService auditLogService,
                                  ObjectMapper objectMapper) {
        this.proposalRepository = proposalRepository;
        this.taskRepository = taskRepository;
        this.governanceService = governanceService;
        this.taskService = taskService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AiActionProposalResponse proposeStatusChange(CreateAiStatusChangeProposalRequest request) {
        User actor = governanceService.currentUserOrThrow();
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new NotFoundException("Task not found"));
        Long workspaceId = task.getSourceDepartment().getWorkspace().getId();
        governanceService.assertWorkspaceAccess(workspaceId);

        AiActionProposal proposal = new AiActionProposal();
        proposal.setWorkspace(task.getSourceDepartment().getWorkspace());
        proposal.setTask(task);
        proposal.setProposedByUser(actor);
        proposal.setActionType(AiActionType.TASK_STATUS_CHANGE);
        proposal.setReason(request.getReason());
        proposal.setPayload(toPayload(request.getTargetStatus()));

        boolean approvalRequired = isHighImpact(request.getTargetStatus());
        proposal.setApprovalRequired(approvalRequired);
        proposal.setStatus(approvalRequired ? AiActionStatus.PENDING_APPROVAL : AiActionStatus.EXECUTED);

        AiActionProposal saved = proposalRepository.save(proposal);
        if (!approvalRequired) {
            executeStatusChange(saved, actor);
        }
        auditLogService.log("AI_ACTION_PROPOSED", "AiActionProposal", saved.getId(), workspaceId,
                Map.of("approvalRequired", approvalRequired, "targetStatus", request.getTargetStatus().name()));
        return AiActionProposalMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AiActionProposalResponse approve(Long proposalId, ReviewAiActionProposalRequest request) {
        if (!(SecurityUtils.hasRole("ADMIN") || SecurityUtils.hasRole("MANAGER") || SecurityUtils.hasRole("TEAM_LEAD"))) {
            throw new AccessDeniedException("Not authorized to approve AI actions");
        }
        User reviewer = governanceService.currentUserOrThrow();
        AiActionProposal proposal = findPendingProposal(proposalId);
        governanceService.assertWorkspaceAccess(proposal.getWorkspace().getId());

        proposal.setReviewedByUser(reviewer);
        proposal.setReviewedAt(Instant.now());
        proposal.setStatus(AiActionStatus.APPROVED);
        if (request != null && request.getComment() != null && !request.getComment().isBlank()) {
            proposal.setReason(proposal.getReason() + " | review: " + request.getComment());
        }
        proposalRepository.save(proposal);
        executeStatusChange(proposal, reviewer);
        auditLogService.log("AI_ACTION_APPROVED", "AiActionProposal", proposal.getId(), proposal.getWorkspace().getId(),
                Map.of("reviewerUserId", reviewer.getId()));
        return AiActionProposalMapper.toResponse(proposal);
    }

    @Override
    @Transactional
    public AiActionProposalResponse reject(Long proposalId, ReviewAiActionProposalRequest request) {
        if (!(SecurityUtils.hasRole("ADMIN") || SecurityUtils.hasRole("MANAGER") || SecurityUtils.hasRole("TEAM_LEAD"))) {
            throw new AccessDeniedException("Not authorized to reject AI actions");
        }
        User reviewer = governanceService.currentUserOrThrow();
        AiActionProposal proposal = findPendingProposal(proposalId);
        governanceService.assertWorkspaceAccess(proposal.getWorkspace().getId());

        proposal.setReviewedByUser(reviewer);
        proposal.setReviewedAt(Instant.now());
        proposal.setStatus(AiActionStatus.REJECTED);
        if (request != null && request.getComment() != null && !request.getComment().isBlank()) {
            proposal.setReason(proposal.getReason() + " | review: " + request.getComment());
        }
        proposalRepository.save(proposal);
        auditLogService.log("AI_ACTION_REJECTED", "AiActionProposal", proposal.getId(), proposal.getWorkspace().getId(),
                Map.of("reviewerUserId", reviewer.getId()));
        return AiActionProposalMapper.toResponse(proposal);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<AiActionProposalResponse> list(int page, int size, Long workspaceId, String status) {
        Specification<AiActionProposal> spec = (root, query, cb) -> cb.conjunction();
        Long scopedWorkspaceId = workspaceId;
        if (!SecurityUtils.hasRole("ADMIN")) {
            if (scopedWorkspaceId == null) {
                scopedWorkspaceId = governanceService.currentPrimaryWorkspaceIdOrThrow();
            }
            governanceService.assertWorkspaceAccess(scopedWorkspaceId);
        }
        if (scopedWorkspaceId != null) {
            Long finalWorkspace = scopedWorkspaceId;
            spec = spec.and((root, query, cb) -> cb.equal(root.get("workspace").get("id"), finalWorkspace));
        }
        if (status != null && !status.isBlank()) {
            AiActionStatus enumStatus = AiActionStatus.valueOf(status);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), enumStatus));
        }
        var result = proposalRepository.findAll(spec, PageRequest.of(page, size));
        var mapped = result.getContent().stream().map(AiActionProposalMapper::toResponse).toList();
        return PaginatedResponse.of(mapped, page, size, result.getTotalElements(), result.getTotalPages());
    }

    private void executeStatusChange(AiActionProposal proposal, User actor) {
        TaskStatus targetStatus = targetStatusFromPayload(proposal.getPayload());
        UpdateTaskStatusRequest update = new UpdateTaskStatusRequest();
        update.setTaskId(proposal.getTask().getId());
        update.setStatus(targetStatus);
        update.setChangedByUserId(actor.getId());
        taskService.updateStatus(update);
        proposal.setStatus(AiActionStatus.EXECUTED);
        proposal.setExecutedAt(Instant.now());
        proposalRepository.save(proposal);
    }

    private AiActionProposal findPendingProposal(Long proposalId) {
        AiActionProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("AI action proposal not found"));
        if (proposal.getStatus() != AiActionStatus.PENDING_APPROVAL) {
            throw new BadRequestException("AI action proposal is not pending approval");
        }
        return proposal;
    }

    private boolean isHighImpact(TaskStatus status) {
        return status == TaskStatus.DONE || status == TaskStatus.CANCELED;
    }

    private String toPayload(TaskStatus status) {
        try {
            return objectMapper.writeValueAsString(Map.of("targetStatus", status.name()));
        } catch (Exception ex) {
            throw new BadRequestException("Failed to build AI action payload");
        }
    }

    private TaskStatus targetStatusFromPayload(String payload) {
        try {
            String value = objectMapper.readTree(payload).get("targetStatus").asText();
            return TaskStatus.valueOf(value);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid AI action payload");
        }
    }
}
