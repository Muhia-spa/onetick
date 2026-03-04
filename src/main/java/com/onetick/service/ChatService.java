package com.onetick.service;

import com.onetick.dto.request.CreateChatChannelRequest;
import com.onetick.dto.request.CreateChatMessageRequest;
import com.onetick.dto.response.ChatChannelResponse;
import com.onetick.dto.response.ChatMessageResponse;

import java.util.List;

public interface ChatService {
    ChatChannelResponse createChannel(CreateChatChannelRequest request);
    List<ChatChannelResponse> listChannels(Long workspaceId);
    ChatMessageResponse postMessage(CreateChatMessageRequest request);
    List<ChatMessageResponse> listMessages(Long channelId);
}
