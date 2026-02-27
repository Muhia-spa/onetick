package com.onetick.dto.request;

import com.onetick.entity.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class CreateUserRequest {
    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(min = 8, max = 120)
    private String password;

    private Long primaryDepartmentId;

    @NotEmpty
    private Set<@NotNull RoleName> roles;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
