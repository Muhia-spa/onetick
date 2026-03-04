package com.onetick.service.impl;

import com.onetick.dto.request.CreateGoalRequest;
import com.onetick.dto.request.UpdateGoalProgressRequest;
import com.onetick.dto.response.GoalResponse;
import com.onetick.entity.Goal;
import com.onetick.entity.User;
import com.onetick.entity.Workspace;
import com.onetick.entity.enums.GoalStatus;
import com.onetick.exception.NotFoundException;
import com.onetick.mapper.GoalMapper;
import com.onetick.repository.GoalRepository;
import com.onetick.repository.UserRepository;
import com.onetick.repository.WorkspaceRepository;
import com.onetick.service.GoalService;
import com.onetick.service.GovernanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final GovernanceService governanceService;

    public GoalServiceImpl(GoalRepository goalRepository,
                           WorkspaceRepository workspaceRepository,
                           UserRepository userRepository,
                           GovernanceService governanceService) {
        this.goalRepository = goalRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.governanceService = governanceService;
    }

    @Override
    @Transactional
    public GoalResponse create(CreateGoalRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
        governanceService.assertWorkspaceAccess(workspace.getId());

        Goal goal = new Goal();
        goal.setWorkspace(workspace);
        goal.setTitle(request.getTitle().trim());
        goal.setDescription(request.getDescription());
        goal.setTargetValue(request.getTargetValue());
        goal.setCurrentValue(0);
        goal.setStatus(GoalStatus.ON_TRACK);

        if (request.getOwnerUserId() != null) {
            User owner = userRepository.findById(request.getOwnerUserId())
                    .orElseThrow(() -> new NotFoundException("Owner user not found"));
            goal.setOwner(owner);
        }

        return GoalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalResponse updateProgress(Long goalId, UpdateGoalProgressRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException("Goal not found"));
        governanceService.assertWorkspaceAccess(goal.getWorkspace().getId());

        goal.setCurrentValue(request.getCurrentValue());
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        } else if (goal.getCurrentValue() >= goal.getTargetValue()) {
            goal.setStatus(GoalStatus.DONE);
        }

        return GoalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> list(Long workspaceId) {
        Long resolvedWorkspaceId = workspaceId;
        if (resolvedWorkspaceId == null) {
            resolvedWorkspaceId = governanceService.currentPrimaryWorkspaceIdOrThrow();
        }
        governanceService.assertWorkspaceAccess(resolvedWorkspaceId);
        return goalRepository.findAllByWorkspaceIdOrderByCreatedAtDesc(resolvedWorkspaceId).stream()
                .map(GoalMapper::toResponse)
                .toList();
    }
}
