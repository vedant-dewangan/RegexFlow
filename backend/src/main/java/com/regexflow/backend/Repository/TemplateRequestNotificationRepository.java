package com.regexflow.backend.Repository;

import com.regexflow.backend.Entity.TemplateRequestNotification;
import com.regexflow.backend.Enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRequestNotificationRepository extends JpaRepository<TemplateRequestNotification, Long> {
    List<TemplateRequestNotification> findByStatus(NotificationStatus status);
    
    List<TemplateRequestNotification> findByStatusOrderByCreatedAtDesc(NotificationStatus status);
}
