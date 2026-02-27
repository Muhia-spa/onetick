package com.onetick.service;

import com.onetick.dto.request.CreateWorkspaceRequest;
import com.onetick.dto.response.WorkspaceResponse;

import java.util.List;

public interface WorkspaceService {
    WorkspaceResponse create(CreateWorkspaceRequest request);
    List<WorkspaceResponse> list();
}
