package com.onetick.mapper;

import com.onetick.dto.response.AiActionProposalResponse;
import com.onetick.entity.AiActionProposal;

public final class AiActionProposalMapper {
    private AiActionProposalMapper() {
    }

    public static AiActionProposalResponse toResponse(AiActionProposal proposal) {
        AiActionProposalResponse response = new AiActionProposalResponse();
        response.setId(proposal.getId());
        response.setWorkspaceId(proposal.getWorkspace().getId());
        response.setTaskId(proposal.getTask().getId());
        response.setProposedByUserId(proposal.getProposedByUser().getId());
        response.setReviewedByUserId(proposal.getReviewedByUser() == null ? null : proposal.getReviewedByUser().getId());
        response.setActionType(proposal.getActionType());
        response.setPayload(proposal.getPayload());
        response.setStatus(proposal.getStatus());
        response.setApprovalRequired(proposal.isApprovalRequired());
        response.setReason(proposal.getReason());
        response.setReviewedAt(proposal.getReviewedAt());
        response.setExecutedAt(proposal.getExecutedAt());
        return response;
    }
}
