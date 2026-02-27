package com.onetick.service.impl;

import com.onetick.dto.request.CreateDepartmentRequest;
import com.onetick.dto.response.DepartmentResponse;
import com.onetick.entity.Department;
import com.onetick.exception.ConflictException;
import com.onetick.mapper.DepartmentMapper;
import com.onetick.repository.DepartmentRepository;
import com.onetick.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);
    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        departmentRepository.findByCode(request.getCode())
                .ifPresent(d -> {
                    throw new ConflictException("Department code already exists");
                });
        Department department = new Department();
        department.setName(request.getName());
        department.setCode(request.getCode());
        Department saved = departmentRepository.save(department);
        log.info("Created department id={}", saved.getId());
        return DepartmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> list() {
        return departmentRepository.findAll().stream()
                .map(DepartmentMapper::toResponse)
                .toList();
    }
}
