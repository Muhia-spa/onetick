package com.onetick.repository;

import com.onetick.entity.WorkspaceDoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceDocRepository extends JpaRepository<WorkspaceDoc, Long> {
    List<WorkspaceDoc> findAllByWorkspaceIdOrderByUpdatedAtDesc(Long workspaceId);
}
