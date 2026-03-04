package com.onetick.controller;

import com.onetick.dto.request.CreateChatChannelRequest;
import com.onetick.dto.request.CreateChatMessageRequest;
import com.onetick.dto.response.ChatChannelResponse;
import com.onetick.dto.response.ChatMessageResponse;
import com.onetick.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/channels")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<ChatChannelResponse> createChannel(@Valid @RequestBody CreateChatChannelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createChannel(request));
    }

    @GetMapping("/channels")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<List<ChatChannelResponse>> listChannels(@RequestParam(required = false) Long workspaceId) {
        return ResponseEntity.ok(chatService.listChannels(workspaceId));
    }

    @PostMapping("/messages")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<ChatMessageResponse> postMessage(@Valid @RequestBody CreateChatMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.postMessage(request));
    }

    @GetMapping("/messages")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<List<ChatMessageResponse>> listMessages(@RequestParam Long channelId) {
        return ResponseEntity.ok(chatService.listMessages(channelId));
    }
}
