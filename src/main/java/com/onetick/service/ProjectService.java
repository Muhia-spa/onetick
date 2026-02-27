package com.onetick.service;

import com.onetick.dto.request.CreateProjectRequest;
import com.onetick.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {
    ProjectResponse create(CreateProjectRequest request);
    List<ProjectResponse> list(Long workspaceId);
}
