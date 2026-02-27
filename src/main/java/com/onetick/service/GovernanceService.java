package com.onetick.service;

import com.onetick.entity.User;

public interface GovernanceService {
    User currentUserOrThrow();
    void assertWorkspaceAccess(Long workspaceId);
    Long currentPrimaryWorkspaceIdOrThrow();
}
