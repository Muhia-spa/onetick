package com.onetick.controller;

import com.onetick.dto.request.CreateAiStatusChangeProposalRequest;
import com.onetick.dto.request.ReviewAiActionProposalRequest;
import com.onetick.dto.response.AiActionProposalResponse;
import com.onetick.dto.response.PaginatedResponse;
import com.onetick.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/actions")
public class AiAssistantController {
    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/status-change")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<AiActionProposalResponse> proposeStatusChange(
            @Valid @RequestBody CreateAiStatusChangeProposalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aiAssistantService.proposeStatusChange(request));
    }

    @PostMapping("/{proposalId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD')")
    public ResponseEntity<AiActionProposalResponse> approve(
            @PathVariable Long proposalId,
            @Valid @RequestBody(required = false) ReviewAiActionProposalRequest request) {
        return ResponseEntity.ok(aiAssistantService.approve(proposalId, request));
    }

    @PostMapping("/{proposalId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD')")
    public ResponseEntity<AiActionProposalResponse> reject(
            @PathVariable Long proposalId,
            @Valid @RequestBody(required = false) ReviewAiActionProposalRequest request) {
        return ResponseEntity.ok(aiAssistantService.reject(proposalId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<PaginatedResponse<AiActionProposalResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long workspaceId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(aiAssistantService.list(page, size, workspaceId, status));
    }
}
