package com.onetick.service;

import com.onetick.dto.request.CreateGoalRequest;
import com.onetick.dto.request.UpdateGoalProgressRequest;
import com.onetick.dto.response.GoalResponse;

import java.util.List;

public interface GoalService {
    GoalResponse create(CreateGoalRequest request);
    GoalResponse updateProgress(Long goalId, UpdateGoalProgressRequest request);
    List<GoalResponse> list(Long workspaceId);
}
