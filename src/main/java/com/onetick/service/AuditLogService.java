package com.onetick.service;

import com.onetick.dto.response.PaginatedResponse;
import com.onetick.entity.AuditLog;

import java.time.Instant;
import java.util.Map;

public interface AuditLogService {
    void log(String action, String entityType, Long entityId, Long workspaceId, Map<String, Object> details);
    PaginatedResponse<AuditLog> list(int page, int size, Long workspaceId, Instant from, Instant to);
}
