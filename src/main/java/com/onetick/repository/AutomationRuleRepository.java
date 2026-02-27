package com.onetick.repository;

import com.onetick.entity.AutomationRule;
import com.onetick.entity.enums.AutomationTriggerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {
    List<AutomationRule> findAllByWorkspaceIdAndActiveTrue(Long workspaceId);
    List<AutomationRule> findAllByWorkspaceIdAndTriggerTypeAndActiveTrue(Long workspaceId, AutomationTriggerType triggerType);
}
