package com.onetick.mapper;

import com.onetick.dto.response.TaskCommentResponse;
import com.onetick.entity.TaskComment;

public final class TaskCommentMapper {
    private TaskCommentMapper() {
    }

    public static TaskCommentResponse toResponse(TaskComment comment) {
        TaskCommentResponse response = new TaskCommentResponse();
        response.setId(comment.getId());
        response.setTaskId(comment.getTask().getId());
        response.setCreatedByUserId(comment.getCreatedBy().getId());
        response.setComment(comment.getComment());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}
