package com.onetick.repository;

import com.onetick.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
}
