package com.onetick.service;

import com.onetick.dto.request.CreateDepartmentRequest;
import com.onetick.dto.response.DepartmentResponse;
import com.onetick.dto.response.PaginatedResponse;

public interface DepartmentService {
    DepartmentResponse create(CreateDepartmentRequest request);
    PaginatedResponse<DepartmentResponse> list(int page, int size, String search, Long workspaceId);
}
