package com.onetick.repository;

import com.onetick.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
    Optional<Department> findByWorkspaceIdAndCode(Long workspaceId, String code);
    Optional<Department> findByWorkspaceIdAndName(Long workspaceId, String name);
}
