package com.onetick.service.impl;

import com.onetick.dto.request.CreateDocRequest;
import com.onetick.dto.request.UpdateDocRequest;
import com.onetick.dto.response.DocResponse;
import com.onetick.entity.User;
import com.onetick.entity.Workspace;
import com.onetick.entity.WorkspaceDoc;
import com.onetick.exception.NotFoundException;
import com.onetick.mapper.DocMapper;
import com.onetick.repository.WorkspaceDocRepository;
import com.onetick.repository.WorkspaceRepository;
import com.onetick.service.DocService;
import com.onetick.service.GovernanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocServiceImpl implements DocService {
    private final WorkspaceDocRepository docRepository;
    private final WorkspaceRepository workspaceRepository;
    private final GovernanceService governanceService;

    public DocServiceImpl(WorkspaceDocRepository docRepository,
                          WorkspaceRepository workspaceRepository,
                          GovernanceService governanceService) {
        this.docRepository = docRepository;
        this.workspaceRepository = workspaceRepository;
        this.governanceService = governanceService;
    }

    @Override
    @Transactional
    public DocResponse create(CreateDocRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
        governanceService.assertWorkspaceAccess(workspace.getId());

        User currentUser = governanceService.currentUserOrThrow();
        WorkspaceDoc doc = new WorkspaceDoc();
        doc.setWorkspace(workspace);
        doc.setTitle(request.getTitle().trim());
        doc.setContent(request.getContent());
        doc.setCreatedBy(currentUser);
        doc.setUpdatedBy(currentUser);
        return DocMapper.toResponse(docRepository.save(doc));
    }

    @Override
    @Transactional
    public DocResponse update(Long docId, UpdateDocRequest request) {
        WorkspaceDoc doc = docRepository.findById(docId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        governanceService.assertWorkspaceAccess(doc.getWorkspace().getId());

        User currentUser = governanceService.currentUserOrThrow();
        doc.setTitle(request.getTitle().trim());
        doc.setContent(request.getContent());
        doc.setUpdatedBy(currentUser);
        return DocMapper.toResponse(docRepository.save(doc));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocResponse> list(Long workspaceId) {
        Long resolvedWorkspaceId = workspaceId;
        if (resolvedWorkspaceId == null) {
            resolvedWorkspaceId = governanceService.currentPrimaryWorkspaceIdOrThrow();
        }
        governanceService.assertWorkspaceAccess(resolvedWorkspaceId);
        return docRepository.findAllByWorkspaceIdOrderByUpdatedAtDesc(resolvedWorkspaceId).stream()
                .map(DocMapper::toResponse)
                .toList();
    }
}
