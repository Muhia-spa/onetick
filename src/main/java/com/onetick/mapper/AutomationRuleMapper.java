package com.onetick.mapper;

import com.onetick.dto.response.AutomationRuleResponse;
import com.onetick.entity.AutomationRule;

public final class AutomationRuleMapper {
    private AutomationRuleMapper() {
    }

    public static AutomationRuleResponse toResponse(AutomationRule rule) {
        AutomationRuleResponse response = new AutomationRuleResponse();
        response.setId(rule.getId());
        response.setWorkspaceId(rule.getWorkspace().getId());
        response.setName(rule.getName());
        response.setTriggerType(rule.getTriggerType());
        response.setActionType(rule.getActionType());
        response.setConditionStatus(rule.getConditionStatus());
        response.setActive(rule.isActive());
        return response;
    }
}
