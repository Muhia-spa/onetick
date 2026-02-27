package com.onetick.dto.response;

import com.onetick.entity.enums.RoleName;

import java.util.Set;

public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private boolean active;
    private Long primaryDepartmentId;
    private Set<RoleName> roles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getPrimaryDepartmentId() {
        return primaryDepartmentId;
    }

    public void setPrimaryDepartmentId(Long primaryDepartmentId) {
        this.primaryDepartmentId = primaryDepartmentId;
    }

    public Set<RoleName> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleName> roles) {
        this.roles = roles;
    }
}
