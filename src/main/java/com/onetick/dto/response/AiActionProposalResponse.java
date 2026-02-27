package com.onetick.dto.response;

import com.onetick.entity.enums.AiActionStatus;
import com.onetick.entity.enums.AiActionType;

import java.time.Instant;

public class AiActionProposalResponse {
    private Long id;
    private Long workspaceId;
    private Long taskId;
    private Long proposedByUserId;
    private Long reviewedByUserId;
    private AiActionType actionType;
    private String payload;
    private AiActionStatus status;
    private boolean approvalRequired;
    private String reason;
    private Instant reviewedAt;
    private Instant executedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getProposedByUserId() {
        return proposedByUserId;
    }

    public void setProposedByUserId(Long proposedByUserId) {
        this.proposedByUserId = proposedByUserId;
    }

    public Long getReviewedByUserId() {
        return reviewedByUserId;
    }

    public void setReviewedByUserId(Long reviewedByUserId) {
        this.reviewedByUserId = reviewedByUserId;
    }

    public AiActionType getActionType() {
        return actionType;
    }

    public void setActionType(AiActionType actionType) {
        this.actionType = actionType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public AiActionStatus getStatus() {
        return status;
    }

    public void setStatus(AiActionStatus status) {
        this.status = status;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }
}
