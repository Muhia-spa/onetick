package com.onetick.repository;

import com.onetick.entity.Department;
import com.onetick.entity.Task;
import com.onetick.entity.User;
import com.onetick.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByTargetDepartment(Department department);
    List<Task> findAllByAssignedTo(User user);
    List<Task> findAllByStatusAndDeadlineBetween(TaskStatus status, Instant from, Instant to);
    List<Task> findAllByStatusInAndDeadlineBetween(Collection<TaskStatus> statuses, Instant from, Instant to);
}
