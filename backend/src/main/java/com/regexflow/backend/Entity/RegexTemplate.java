package com.regexflow.backend.Entity;

import com.regexflow.backend.Enums.PaymentType;
import com.regexflow.backend.Enums.RegexTemplateStatus;
import com.regexflow.backend.Enums.SmsType;
import com.regexflow.backend.Enums.TransactionType;
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

    @Lob
    @Column(name = "sample_raw_msg")
    private String sampleRawMsg;

    @Enumerated(EnumType.STRING)
    @Column(name = "sms_type",nullable=false)
    private SmsType smsType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type",nullable=false)
    private TransactionType transactionType;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

}
