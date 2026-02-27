package com.onetick.service.impl;

import com.onetick.dto.request.CreateProjectRequest;
import com.onetick.dto.response.ProjectResponse;
import com.onetick.entity.Project;
import com.onetick.entity.Workspace;
import com.onetick.exception.ConflictException;
import com.onetick.exception.NotFoundException;
import com.onetick.mapper.ProjectMapper;
import com.onetick.repository.ProjectRepository;
import com.onetick.repository.WorkspaceRepository;
import com.onetick.service.AuditLogService;
import com.onetick.service.GovernanceService;
import com.onetick.service.ProjectService;
import com.onetick.util.SecurityUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final GovernanceService governanceService;
    private final AuditLogService auditLogService;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              WorkspaceRepository workspaceRepository,
                              GovernanceService governanceService,
                              AuditLogService auditLogService) {
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
        this.governanceService = governanceService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
        governanceService.assertWorkspaceAccess(workspace.getId());
        projectRepository.findByWorkspaceIdAndCode(workspace.getId(), request.getCode())
                .ifPresent(existing -> {
                    throw new ConflictException("Project code already exists in workspace");
                });
        Project project = new Project();
        project.setWorkspace(workspace);
        project.setName(request.getName());
        project.setCode(request.getCode());
        Project saved = projectRepository.save(project);
        auditLogService.log("PROJECT_CREATE", "Project", saved.getId(), workspace.getId(),
                java.util.Map.of("code", saved.getCode(), "name", saved.getName()));
        return ProjectMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> list(Long workspaceId) {
        Long scopedWorkspaceId = workspaceId;
        if (!SecurityUtils.hasRole("ADMIN")) {
            if (scopedWorkspaceId == null) {
                scopedWorkspaceId = governanceService.currentPrimaryWorkspaceIdOrThrow();
            }
            governanceService.assertWorkspaceAccess(scopedWorkspaceId);
        }
        Specification<Project> spec = (root, query, cb) -> cb.conjunction();
        if (scopedWorkspaceId != null) {
            Long finalWorkspaceId = scopedWorkspaceId;
            spec = spec.and((root, query, cb) -> cb.equal(root.get("workspace").get("id"), finalWorkspaceId));
        }
        return projectRepository.findAll(spec).stream()
                .map(ProjectMapper::toResponse)
                .toList();
    }
}
