package com.onetick.mapper;

import com.onetick.dto.response.AuditLogResponse;
import com.onetick.entity.AuditLog;

public final class AuditLogMapper {
    private AuditLogMapper() {
    }

    public static AuditLogResponse toResponse(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setActorUserId(log.getActorUser() == null ? null : log.getActorUser().getId());
        response.setActorEmail(log.getActorEmail());
        response.setAction(log.getAction());
        response.setEntityType(log.getEntityType());
        response.setEntityId(log.getEntityId());
        response.setWorkspaceId(log.getWorkspace() == null ? null : log.getWorkspace().getId());
        response.setRequestMethod(log.getRequestMethod());
        response.setRequestPath(log.getRequestPath());
        response.setCorrelationId(log.getCorrelationId());
        response.setDetails(log.getDetails());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
