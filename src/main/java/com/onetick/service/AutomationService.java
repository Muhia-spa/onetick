package com.onetick.service;

import com.onetick.dto.request.CreateAutomationRuleRequest;
import com.onetick.dto.response.AutomationRuleResponse;
import com.onetick.entity.Task;
import com.onetick.entity.enums.TaskStatus;

import java.util.List;

public interface AutomationService {
    AutomationRuleResponse createRule(CreateAutomationRuleRequest request);
    List<AutomationRuleResponse> listRules(Long workspaceId);
    void onTaskCreated(Task task);
    void onTaskStatusChanged(Task task, TaskStatus oldStatus, TaskStatus newStatus);
}
