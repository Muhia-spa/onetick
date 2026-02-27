package com.onetick.mapper;

import com.onetick.dto.response.UserResponse;
import com.onetick.entity.Role;
import com.onetick.entity.User;
import com.onetick.entity.enums.RoleName;

import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setActive(user.isActive());
        response.setPrimaryDepartmentId(
                user.getPrimaryDepartment() == null ? null : user.getPrimaryDepartment().getId()
        );
        Set<RoleName> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        response.setRoles(roles);
        return response;
    }
}
