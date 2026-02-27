package com.onetick.dto.request;

import jakarta.validation.constraints.Size;

public class ReviewAiActionProposalRequest {
    @Size(max = 1000)
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
