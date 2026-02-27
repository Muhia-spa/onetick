package com.onetick.controller;

import com.onetick.dto.request.CreateProjectRequest;
import com.onetick.dto.response.ProjectResponse;
import com.onetick.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD')")
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<List<ProjectResponse>> list(@RequestParam(required = false) Long workspaceId) {
        return ResponseEntity.ok(projectService.list(workspaceId));
    }
}
