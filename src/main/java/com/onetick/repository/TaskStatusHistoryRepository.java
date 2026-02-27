package com.onetick.repository;

import com.onetick.entity.TaskStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory, Long> {
}
