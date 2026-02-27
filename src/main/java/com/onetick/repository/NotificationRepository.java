package com.onetick.repository;

import com.onetick.entity.Notification;
import com.onetick.entity.enums.NotificationStatus;
import com.onetick.entity.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByStatusAndType(NotificationStatus status, NotificationType type);
}
