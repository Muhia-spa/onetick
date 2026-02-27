package com.onetick.service.impl;

import com.onetick.dto.request.CreateWorkspaceRequest;
import com.onetick.dto.response.WorkspaceResponse;
import com.onetick.entity.Workspace;
import com.onetick.exception.ConflictException;
import com.onetick.mapper.WorkspaceMapper;
import com.onetick.repository.WorkspaceRepository;
import com.onetick.service.WorkspaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    private final WorkspaceRepository workspaceRepository;

    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional
    public WorkspaceResponse create(CreateWorkspaceRequest request) {
        workspaceRepository.findByCode(request.getCode())
                .ifPresent(existing -> {
                    throw new ConflictException("Workspace code already exists");
                });
        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setCode(request.getCode());
        return WorkspaceMapper.toResponse(workspaceRepository.save(workspace));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> list() {
        return workspaceRepository.findAll().stream()
                .map(WorkspaceMapper::toResponse)
                .toList();
    }
}
