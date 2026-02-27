package com.onetick.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onetick.config.RequestCorrelationFilter;
import com.onetick.dto.response.PaginatedResponse;
import com.onetick.entity.AuditLog;
import com.onetick.entity.User;
import com.onetick.entity.Workspace;
import com.onetick.repository.AuditLogRepository;
import com.onetick.repository.WorkspaceRepository;
import com.onetick.service.AuditLogService;
import com.onetick.service.GovernanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Map;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final WorkspaceRepository workspaceRepository;
    private final GovernanceService governanceService;
    private final ObjectMapper objectMapper;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository,
                               WorkspaceRepository workspaceRepository,
                               GovernanceService governanceService,
                               ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.workspaceRepository = workspaceRepository;
        this.governanceService = governanceService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void log(String action, String entityType, Long entityId, Long workspaceId, Map<String, Object> details) {
        AuditLog log = new AuditLog();
        User actor = governanceService.currentUserOrThrow();
        log.setActorUser(actor);
        log.setActorEmail(actor.getEmail());
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        if (workspaceId != null) {
            Workspace workspace = workspaceRepository.findById(workspaceId).orElse(null);
            log.setWorkspace(workspace);
        }

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            log.setRequestMethod(request.getMethod());
            log.setRequestPath(request.getRequestURI());
        }
        log.setCorrelationId(MDC.get(RequestCorrelationFilter.MDC_KEY));
        try {
            log.setDetails(objectMapper.writeValueAsString(details));
        } catch (Exception e) {
            log.setDetails("{\"serialization\":\"failed\"}");
        }
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<AuditLog> list(int page, int size, Long workspaceId, Instant from, Instant to) {
        Specification<AuditLog> spec = (root, query, cb) -> cb.conjunction();
        if (workspaceId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        var result = auditLogRepository.findAll(spec, PageRequest.of(page, size));
        return PaginatedResponse.of(result.getContent(), page, size, result.getTotalElements(), result.getTotalPages());
    }
}
