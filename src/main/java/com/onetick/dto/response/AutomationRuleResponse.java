package com.onetick.dto.response;

import com.onetick.entity.enums.AutomationActionType;
import com.onetick.entity.enums.AutomationTriggerType;
import com.onetick.entity.enums.TaskStatus;

public class AutomationRuleResponse {
    private Long id;
    private Long workspaceId;
    private String name;
    private AutomationTriggerType triggerType;
    private AutomationActionType actionType;
    private TaskStatus conditionStatus;
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
