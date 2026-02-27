package com.onetick.repository;

import com.onetick.entity.Department;
import com.onetick.entity.DepartmentMember;
import com.onetick.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentMemberRepository extends JpaRepository<DepartmentMember, Long> {
    Optional<DepartmentMember> findByDepartmentAndUser(Department department, User user);
    List<DepartmentMember> findAllByDepartment(Department department);
    boolean existsByUserIdAndDepartmentWorkspaceId(Long userId, Long workspaceId);
}
