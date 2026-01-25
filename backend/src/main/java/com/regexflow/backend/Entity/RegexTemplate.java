package com.regexflow.backend.Entity;

import com.regexflow.backend.Enums.RegexTemplateStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="regex_templates")
@Getter
@Setter
public class RegexTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id",nullable = false)
    private Long templateId;

    @Column(name="sender_header",nullable=false)
    private String senderHeader;

    @Lob
    @Column(nullable=false)
    private String pattern;

    @Column(name = "sms_type",nullable=false)
    private String smsType;


    // Current lifecycle state: DRAFT, PENDING, VERIFIED, DEPRECATED
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private RegexTemplateStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = RegexTemplateStatus.DRAFT;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name="bank_id")
    private Bank bank;

    @OneToOne(mappedBy = "template")
    private AuditLog auditLog;

}
