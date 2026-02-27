package com.onetick.service.impl;

import com.onetick.dto.request.CreateAutomationRuleRequest;
import com.onetick.dto.response.AutomationRuleResponse;
import com.onetick.entity.AutomationRule;
import com.onetick.entity.Task;
import com.onetick.entity.Workspace;
import com.onetick.entity.enums.AutomationActionType;
import com.onetick.entity.enums.AutomationTriggerType;
import com.onetick.entity.enums.NotificationType;
import com.onetick.entity.enums.TaskStatus;
import com.onetick.exception.NotFoundException;
import com.onetick.mapper.AutomationRuleMapper;
import com.onetick.repository.AutomationRuleRepository;
import com.onetick.repository.WorkspaceRepository;
import com.onetick.service.AuditLogService;
import com.onetick.service.AutomationService;
import com.onetick.service.GovernanceService;
import com.onetick.service.NotificationService;
import com.onetick.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AutomationServiceImpl implements AutomationService {
    private final AutomationRuleRepository automationRuleRepository;
    private final WorkspaceRepository workspaceRepository;
    private final GovernanceService governanceService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public AutomationServiceImpl(AutomationRuleRepository automationRuleRepository,
                                 WorkspaceRepository workspaceRepository,
                                 GovernanceService governanceService,
                                 NotificationService notificationService,
                                 AuditLogService auditLogService) {
        this.automationRuleRepository = automationRuleRepository;
        this.workspaceRepository = workspaceRepository;
        this.governanceService = governanceService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public AutomationRuleResponse createRule(CreateAutomationRuleRequest request) {
        governanceService.assertWorkspaceAccess(request.getWorkspaceId());
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new NotFoundException("Workspace not found"));

        AutomationRule rule = new AutomationRule();
        rule.setWorkspace(workspace);
        rule.setName(request.getName());
        rule.setTriggerType(request.getTriggerType());
        rule.setActionType(request.getActionType());
        rule.setConditionStatus(request.getConditionStatus());
        rule.setActive(true);
        AutomationRule saved = automationRuleRepository.save(rule);
        auditLogService.log("AUTOMATION_RULE_CREATE", "AutomationRule", saved.getId(), workspace.getId(),
                Map.of("triggerType", saved.getTriggerType().name(), "actionType", saved.getActionType().name()));
        return AutomationRuleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutomationRuleResponse> listRules(Long workspaceId) {
        Long scopedWorkspace = workspaceId;
        if (!SecurityUtils.hasRole("ADMIN")) {
            if (scopedWorkspace == null) {
                scopedWorkspace = governanceService.currentPrimaryWorkspaceIdOrThrow();
            }
            governanceService.assertWorkspaceAccess(scopedWorkspace);
        }
        List<AutomationRule> rules = scopedWorkspace == null
                ? automationRuleRepository.findAll()
                : automationRuleRepository.findAllByWorkspaceIdAndActiveTrue(scopedWorkspace);
        return rules.stream().map(AutomationRuleMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void onTaskCreated(Task task) {
        executeRules(task, AutomationTriggerType.TASK_CREATED, null, task.getStatus());
    }

    @Override
    @Transactional
    public void onTaskStatusChanged(Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        executeRules(task, AutomationTriggerType.TASK_STATUS_CHANGED, oldStatus, newStatus);
    }

    private void executeRules(Task task, AutomationTriggerType trigger, TaskStatus oldStatus, TaskStatus newStatus) {
        Long workspaceId = task.getSourceDepartment().getWorkspace().getId();
        List<AutomationRule> rules = automationRuleRepository
                .findAllByWorkspaceIdAndTriggerTypeAndActiveTrue(workspaceId, trigger);
        for (AutomationRule rule : rules) {
            if (rule.getConditionStatus() != null && rule.getConditionStatus() != newStatus) {
                continue;
            }
            if (rule.getActionType() == AutomationActionType.SEND_ASSIGNEE_REMINDER && task.getAssignedTo() != null) {
                notificationService.notifyUser(task, task.getAssignedTo(), NotificationType.REMINDER);
                auditLogService.log("AUTOMATION_RULE_EXECUTED", "AutomationRule", rule.getId(), workspaceId,
                        Map.of("taskId", task.getId(), "trigger", trigger.name(),
                                "oldStatus", oldStatus == null ? "null" : oldStatus.name(),
                                "newStatus", newStatus.name()));
            }
        }
    }
}
