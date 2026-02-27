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
import com.onetick.service.ProjectService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              WorkspaceRepository workspaceRepository) {
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
        projectRepository.findByWorkspaceIdAndCode(workspace.getId(), request.getCode())
                .ifPresent(existing -> {
                    throw new ConflictException("Project code already exists in workspace");
                });
        Project project = new Project();
        project.setWorkspace(workspace);
        project.setName(request.getName());
        project.setCode(request.getCode());
        return ProjectMapper.toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> list(Long workspaceId) {
        Specification<Project> spec = (root, query, cb) -> cb.conjunction();
        if (workspaceId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId));
        }
        return projectRepository.findAll(spec).stream()
                .map(ProjectMapper::toResponse)
                .toList();
    }
}
