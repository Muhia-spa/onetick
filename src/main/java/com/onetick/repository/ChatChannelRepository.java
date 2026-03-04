package com.onetick.repository;

import com.onetick.entity.ChatChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {
    List<ChatChannel> findAllByWorkspaceIdAndActiveTrueOrderByCreatedAtAsc(Long workspaceId);
}
