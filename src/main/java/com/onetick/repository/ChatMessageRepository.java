package com.onetick.repository;

import com.onetick.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop100ByChannelIdOrderByCreatedAtDesc(Long channelId);
}
