package com.onetick.service;

import com.onetick.dto.request.CreateDepartmentRequest;
import com.onetick.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(CreateDepartmentRequest request);
    List<DepartmentResponse> list();
}
