package com.onetick.service.impl;

import com.onetick.dto.request.CreateUserRequest;
import com.onetick.dto.response.UserResponse;
import com.onetick.entity.Department;
import com.onetick.entity.Role;
import com.onetick.entity.User;
import com.onetick.entity.enums.RoleName;
import com.onetick.exception.ConflictException;
import com.onetick.exception.NotFoundException;
import com.onetick.mapper.UserMapper;
import com.onetick.repository.DepartmentRepository;
import com.onetick.repository.RoleRepository;
import com.onetick.repository.UserRepository;
import com.onetick.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           DepartmentRepository departmentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new ConflictException("Email already in use");
                });

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if (request.getPrimaryDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getPrimaryDepartmentId())
                    .orElseThrow(() -> new NotFoundException("Department not found"));
            user.setPrimaryDepartment(department);
        }
        Set<Role> roles = new HashSet<>();
        for (RoleName roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
            roles.add(role);
        }
        user.setRoles(roles);

        User saved = userRepository.save(user);
        log.info("Created user id={}", saved.getId());
        return UserMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }
}
