package com.onetick.service.impl;

import com.onetick.dto.request.CreateDepartmentRequest;
import com.onetick.dto.response.DepartmentResponse;
import com.onetick.dto.response.PaginatedResponse;
import com.onetick.entity.Department;
import com.onetick.entity.Workspace;
import com.onetick.exception.ConflictException;
import com.onetick.exception.NotFoundException;
import com.onetick.mapper.DepartmentMapper;
import com.onetick.repository.DepartmentRepository;
import com.onetick.repository.WorkspaceRepository;
import com.onetick.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);
    private final DepartmentRepository departmentRepository;
    private final WorkspaceRepository workspaceRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 WorkspaceRepository workspaceRepository) {
        this.departmentRepository = departmentRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new NotFoundException("Workspace not found"));

        departmentRepository.findByWorkspaceIdAndCode(workspace.getId(), request.getCode())
                .ifPresent(d -> {
                    throw new ConflictException("Department code already exists");
                });
        departmentRepository.findByWorkspaceIdAndName(workspace.getId(), request.getName())
                .ifPresent(d -> {
                    throw new ConflictException("Department name already exists");
                });
        Department department = new Department();
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setWorkspace(workspace);
        Department saved = departmentRepository.save(department);
        log.info("Created department id={}", saved.getId());
        return DepartmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<DepartmentResponse> list(int page, int size, String search, Long workspaceId) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Department> spec = (root, query, cb) -> cb.conjunction();

        if (workspaceId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId));
        }
        if (search != null && !search.isBlank()) {
            String term = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), term),
                    cb.like(cb.lower(root.get("code")), term)
            ));
        }

        var result = departmentRepository.findAll(spec, pageable);
        var items = result.getContent().stream()
                .map(DepartmentMapper::toResponse)
                .toList();
        return PaginatedResponse.of(items, page, size, result.getTotalElements(), result.getTotalPages());
    }
}
