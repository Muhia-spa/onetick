package com.onetick.controller;

import com.onetick.dto.request.CreateGoalRequest;
import com.onetick.dto.request.UpdateGoalProgressRequest;
import com.onetick.dto.response.GoalResponse;
import com.onetick.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD')")
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody CreateGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(request));
    }

    @PatchMapping("/{goalId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD')")
    public ResponseEntity<GoalResponse> updateProgress(@PathVariable Long goalId,
                                                       @Valid @RequestBody UpdateGoalProgressRequest request) {
        return ResponseEntity.ok(goalService.updateProgress(goalId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<List<GoalResponse>> list(@RequestParam(required = false) Long workspaceId) {
        return ResponseEntity.ok(goalService.list(workspaceId));
    }
}
