package com.onetick.repository;

import com.onetick.entity.Department;
import com.onetick.entity.Task;
import com.onetick.entity.User;
import com.onetick.entity.enums.TaskStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    @Override
    @Cacheable(cacheNames = "tasks", key = "#id")
    Optional<Task> findById(Long id);

    @Override
    @CachePut(cacheNames = "tasks", key = "#result.id")
    <S extends Task> S save(S entity);

    @Override
    @CacheEvict(cacheNames = "tasks", key = "#id")
    void deleteById(Long id);

    List<Task> findAllByTargetDepartment(Department department);
    List<Task> findAllByAssignedTo(User user);
    List<Task> findAllByStatusAndDeadlineBetween(TaskStatus status, Instant from, Instant to);
    List<Task> findAllByStatusInAndDeadlineBetween(Collection<TaskStatus> statuses, Instant from, Instant to);
}
