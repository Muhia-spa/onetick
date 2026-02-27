package com.onetick.dto.request;

import com.onetick.entity.enums.AutomationActionType;
import com.onetick.entity.enums.AutomationTriggerType;
import com.onetick.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateAutomationRuleRequest {
    @NotNull
    private Long workspaceId;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private AutomationTriggerType triggerType;

    @NotNull
    private AutomationActionType actionType;

    private TaskStatus conditionStatus;

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AutomationTriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(AutomationTriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public AutomationActionType getActionType() {
        return actionType;
    }

    public void setActionType(AutomationActionType actionType) {
        this.actionType = actionType;
    }

    public TaskStatus getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(TaskStatus conditionStatus) {
        this.conditionStatus = conditionStatus;
    }
}
