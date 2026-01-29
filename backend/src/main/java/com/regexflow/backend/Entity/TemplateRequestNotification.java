package com.regexflow.backend.Entity;

import com.regexflow.backend.Enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "template_request_notifications")
@Getter
@Setter
public class TemplateRequestNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sms_id", nullable = false)
    private Sms sms;

    @Lob
    @Column(name = "sms_text", nullable = false)
    private String smsText;

    @Column(name = "sender_header", nullable = false)
    private String senderHeader;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private Users requestedBy; // Customer who requested the template

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = NotificationStatus.PENDING;
        }
    }
}
