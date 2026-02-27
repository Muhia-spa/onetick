package com.onetick.entity;

import com.onetick.entity.enums.AutomationActionType;
import com.onetick.entity.enums.AutomationTriggerType;
import com.onetick.entity.enums.TaskStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "automation_rules")
public class AutomationRule extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 50)
    private AutomationTriggerType triggerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private AutomationActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status", length = 30)
    private TaskStatus conditionStatus;

    @Column(nullable = false)
    private boolean active = true;

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
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
