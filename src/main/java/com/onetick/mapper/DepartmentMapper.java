package com.onetick.mapper;

import com.onetick.dto.response.DepartmentResponse;
import com.onetick.entity.Department;

public final class DepartmentMapper {
    private DepartmentMapper() {
    }

    public static DepartmentResponse toResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setCode(department.getCode());
        response.setActive(department.isActive());
        response.setWorkspaceId(department.getWorkspace().getId());
        return response;
    }
}
