package com.onetick.mapper;

import com.onetick.dto.response.ChatChannelResponse;
import com.onetick.entity.ChatChannel;

public final class ChatChannelMapper {
    private ChatChannelMapper() {
    }

    public static ChatChannelResponse toResponse(ChatChannel channel) {
        ChatChannelResponse response = new ChatChannelResponse();
        response.setId(channel.getId());
        response.setWorkspaceId(channel.getWorkspace().getId());
        response.setName(channel.getName());
        response.setTopic(channel.getTopic());
        response.setActive(channel.isActive());
        response.setCreatedByUserId(channel.getCreatedBy().getId());
        return response;
    }
}
