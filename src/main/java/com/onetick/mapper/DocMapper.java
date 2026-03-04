package com.onetick.mapper;

import com.onetick.dto.response.DocResponse;
import com.onetick.entity.WorkspaceDoc;

public final class DocMapper {
    private DocMapper() {
    }

    public static DocResponse toResponse(WorkspaceDoc doc) {
        DocResponse response = new DocResponse();
        response.setId(doc.getId());
        response.setWorkspaceId(doc.getWorkspace().getId());
        response.setTitle(doc.getTitle());
        response.setContent(doc.getContent());
        response.setCreatedByUserId(doc.getCreatedBy().getId());
        response.setUpdatedByUserId(doc.getUpdatedBy().getId());
        response.setUpdatedAt(doc.getUpdatedAt());
        return response;
    }
}
