package com.onetick.mapper;

import com.onetick.dto.response.WorkspaceResponse;
import com.onetick.entity.Workspace;

public final class WorkspaceMapper {
    private WorkspaceMapper() {
    }

    public static WorkspaceResponse toResponse(Workspace workspace) {
        WorkspaceResponse response = new WorkspaceResponse();
        response.setId(workspace.getId());
        response.setName(workspace.getName());
        response.setCode(workspace.getCode());
        response.setActive(workspace.isActive());
        return response;
    }
}
