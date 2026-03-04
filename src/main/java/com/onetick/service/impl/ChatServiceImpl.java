package com.onetick.service.impl;

import com.onetick.dto.request.CreateChatChannelRequest;
import com.onetick.dto.request.CreateChatMessageRequest;
import com.onetick.dto.response.ChatChannelResponse;
import com.onetick.dto.response.ChatMessageResponse;
import com.onetick.entity.ChatChannel;
import com.onetick.entity.ChatMessage;
import com.onetick.entity.User;
import com.onetick.entity.Workspace;
import com.onetick.exception.NotFoundException;
import com.onetick.mapper.ChatChannelMapper;
import com.onetick.mapper.ChatMessageMapper;
import com.onetick.repository.ChatChannelRepository;
import com.onetick.repository.ChatMessageRepository;
import com.onetick.repository.WorkspaceRepository;
import com.onetick.service.ChatService;
import com.onetick.service.GovernanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatChannelRepository channelRepository;
    private final ChatMessageRepository messageRepository;
    private final WorkspaceRepository workspaceRepository;
    private final GovernanceService governanceService;

    public ChatServiceImpl(ChatChannelRepository channelRepository,
                           ChatMessageRepository messageRepository,
                           WorkspaceRepository workspaceRepository,
                           GovernanceService governanceService) {
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
        this.workspaceRepository = workspaceRepository;
        this.governanceService = governanceService;
    }

    @Override
    @Transactional
    public ChatChannelResponse createChannel(CreateChatChannelRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new NotFoundException("Workspace not found"));
        governanceService.assertWorkspaceAccess(workspace.getId());

        User currentUser = governanceService.currentUserOrThrow();
        ChatChannel channel = new ChatChannel();
        channel.setWorkspace(workspace);
        channel.setName(request.getName().trim());
        channel.setTopic(request.getTopic());
        channel.setCreatedBy(currentUser);
        channel.setActive(true);

        return ChatChannelMapper.toResponse(channelRepository.save(channel));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatChannelResponse> listChannels(Long workspaceId) {
        Long resolvedWorkspaceId = workspaceId;
        if (resolvedWorkspaceId == null) {
            resolvedWorkspaceId = governanceService.currentPrimaryWorkspaceIdOrThrow();
        }
        governanceService.assertWorkspaceAccess(resolvedWorkspaceId);

        return channelRepository.findAllByWorkspaceIdAndActiveTrueOrderByCreatedAtAsc(resolvedWorkspaceId).stream()
                .map(ChatChannelMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChatMessageResponse postMessage(CreateChatMessageRequest request) {
        ChatChannel channel = channelRepository.findById(request.getChannelId())
                .orElseThrow(() -> new NotFoundException("Chat channel not found"));
        governanceService.assertWorkspaceAccess(channel.getWorkspace().getId());

        User currentUser = governanceService.currentUserOrThrow();
        ChatMessage message = new ChatMessage();
        message.setChannel(channel);
        message.setSender(currentUser);
        message.setMessage(request.getMessage().trim());
        return ChatMessageMapper.toResponse(messageRepository.save(message));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> listMessages(Long channelId) {
        ChatChannel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Chat channel not found"));
        governanceService.assertWorkspaceAccess(channel.getWorkspace().getId());

        return messageRepository.findTop100ByChannelIdOrderByCreatedAtDesc(channelId).stream()
                .map(ChatMessageMapper::toResponse)
                .sorted(Comparator.comparing(ChatMessageResponse::getCreatedAt))
                .toList();
    }
}
