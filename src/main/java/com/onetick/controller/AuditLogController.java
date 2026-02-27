package com.onetick.controller;

import com.onetick.dto.response.PaginatedResponse;
import com.onetick.dto.response.AuditLogResponse;
import com.onetick.mapper.AuditLogMapper;
import com.onetick.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<AuditLogResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Long workspaceId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        PaginatedResponse<com.onetick.entity.AuditLog> logs = auditLogService.list(page, size, workspaceId, from, to);
        return ResponseEntity.ok(PaginatedResponse.of(
                logs.getItems().stream().map(AuditLogMapper::toResponse).toList(),
                logs.getPage(),
                logs.getSize(),
                logs.getTotalElements(),
                logs.getTotalPages()
        ));
    }
}
