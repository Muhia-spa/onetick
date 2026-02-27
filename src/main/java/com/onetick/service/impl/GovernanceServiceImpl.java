package com.onetick.service.impl;

import com.onetick.entity.Department;
import com.onetick.entity.User;
import com.onetick.exception.BadRequestException;
import com.onetick.exception.NotFoundException;
import com.onetick.repository.DepartmentMemberRepository;
import com.onetick.repository.UserRepository;
import com.onetick.service.GovernanceService;
import com.onetick.util.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GovernanceServiceImpl implements GovernanceService {
    private final UserRepository userRepository;
    private final DepartmentMemberRepository departmentMemberRepository;

    public GovernanceServiceImpl(UserRepository userRepository,
                                 DepartmentMemberRepository departmentMemberRepository) {
        this.userRepository = userRepository;
        this.departmentMemberRepository = departmentMemberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User currentUserOrThrow() {
        String email = SecurityUtils.currentUsername();
        if (email == null) {
            throw new BadRequestException("Unauthenticated request");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public void assertWorkspaceAccess(Long workspaceId) {
        if (workspaceId == null) {
            throw new BadRequestException("workspaceId is required");
        }
        if (SecurityUtils.hasRole("ADMIN")) {
            return;
        }

        User user = currentUserOrThrow();
        Department primary = user.getPrimaryDepartment();
        boolean hasPrimaryWorkspace = primary != null && primary.getWorkspace().getId().equals(workspaceId);
        boolean hasMemberWorkspace = departmentMemberRepository.existsByUserIdAndDepartmentWorkspaceId(user.getId(), workspaceId);

        if (!hasPrimaryWorkspace && !hasMemberWorkspace) {
            throw new AccessDeniedException("Workspace access denied");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long currentPrimaryWorkspaceIdOrThrow() {
        User user = currentUserOrThrow();
        Department primary = user.getPrimaryDepartment();
        if (primary == null) {
            throw new AccessDeniedException("User is not assigned to a primary workspace");
        }
        return primary.getWorkspace().getId();
    }
}
