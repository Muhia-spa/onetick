package com.onetick.mapper;

import com.onetick.dto.response.ProjectResponse;
import com.onetick.entity.Project;

public final class ProjectMapper {
    private ProjectMapper() {
    }

    public static ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setWorkspaceId(project.getWorkspace().getId());
        response.setName(project.getName());
        response.setCode(project.getCode());
        response.setActive(project.isActive());
        return response;
    }
}
