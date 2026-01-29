package com.regexflow.backend.Dto;

import com.regexflow.backend.Enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequestNotificationDto {
    private Long notificationId;
    private Long smsId;
    private String smsText;
    private String senderHeader;
    private Long requestedById;
    private String requestedByName;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
