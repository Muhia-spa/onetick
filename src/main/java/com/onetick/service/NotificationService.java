package com.onetick.service;

import com.onetick.entity.Task;
import com.onetick.entity.User;
import com.onetick.entity.enums.NotificationType;

public interface NotificationService {
    void notifyUser(Task task, User recipient, NotificationType type);
}
