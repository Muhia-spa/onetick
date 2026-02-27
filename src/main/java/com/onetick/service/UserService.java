package com.onetick.service;

import com.onetick.dto.request.CreateUserRequest;
import com.onetick.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse create(CreateUserRequest request);
    List<UserResponse> list();
}
