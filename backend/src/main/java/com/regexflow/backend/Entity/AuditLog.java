package com.regexflow.backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

import com.regexflow.backend.Enums.AuditStatus;

@Entity
@Table(name="audit_logs")
@Getter
@Setter
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="audit_id")
    private Long auditId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditStatus status;

   
    @ManyToOne(optional = false)
    @JoinColumn(name = "verified_by")
    private Users verifiedBy;


    @Column(nullable = false,name="verified_at")
    private LocalDateTime verifiedAt;

    @PrePersist
    public void prePersist(){
        this.verifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate(){
        this.verifiedAt = LocalDateTime.now();
    }


    @OneToOne(optional = false)
    @JoinColumn(name = "template_id",unique = true)
    private RegexTemplate template;

}
