package Entity;

import Enums.RegexTemplateStatus;
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
    private Long TemplateId;

    @Column(name="sender_header",nullable=false)
    private String SenderHeader;

    @Lob
    @Column(nullable=false)
    private String Pattern;

    @Column(name = "sms_type",nullable=false)
    private String SmsType;


    // Current lifecycle state: DRAFT, PENDING, VERIFIED, DEPRECATED
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private RegexTemplateStatus Status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by")
    private Users CreatedBy;

    @Column(name = "created_at")
    private LocalDateTime CreatedAt;

    @PrePersist
    public void prePersist(){
        this.CreatedAt = LocalDateTime.now();
        this.Status = RegexTemplateStatus.DRAFT;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name="bank_id")
    private Bank Bank;

    @OneToOne(mappedBy = "Template")
    private AuditLog AuditLog;

}
