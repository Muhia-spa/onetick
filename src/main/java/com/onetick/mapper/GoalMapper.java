package com.onetick.mapper;

import com.onetick.dto.response.GoalResponse;
import com.onetick.entity.Goal;

public final class GoalMapper {
    private GoalMapper() {
    }

    public static GoalResponse toResponse(Goal goal) {
        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setWorkspaceId(goal.getWorkspace().getId());
        response.setTitle(goal.getTitle());
        response.setDescription(goal.getDescription());
        response.setTargetValue(goal.getTargetValue());
        response.setCurrentValue(goal.getCurrentValue());
        response.setStatus(goal.getStatus());
        response.setOwnerUserId(goal.getOwner() == null ? null : goal.getOwner().getId());
        return response;
    }
}
