package com.onetick.service;

import com.onetick.dto.request.CreateAiStatusChangeProposalRequest;
import com.onetick.dto.request.ReviewAiActionProposalRequest;
import com.onetick.dto.response.AiActionProposalResponse;
import com.onetick.dto.response.PaginatedResponse;

public interface AiAssistantService {
    AiActionProposalResponse proposeStatusChange(CreateAiStatusChangeProposalRequest request);
    AiActionProposalResponse approve(Long proposalId, ReviewAiActionProposalRequest request);
    AiActionProposalResponse reject(Long proposalId, ReviewAiActionProposalRequest request);
    PaginatedResponse<AiActionProposalResponse> list(int page, int size, Long workspaceId, String status);
}
