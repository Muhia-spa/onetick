package com.onetick.mapper;

import com.onetick.dto.response.ChatMessageResponse;
import com.onetick.entity.ChatMessage;

public final class ChatMessageMapper {
    private ChatMessageMapper() {
    }

    public static ChatMessageResponse toResponse(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setChannelId(message.getChannel().getId());
        response.setSenderUserId(message.getSender().getId());
        response.setSenderName(message.getSender().getName());
        response.setMessage(message.getMessage());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
