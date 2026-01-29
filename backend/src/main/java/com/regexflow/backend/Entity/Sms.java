package com.regexflow.backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms")
@Getter
@Setter
public class Sms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sms_id", nullable = false)
    private Long smsId;

    @Lob
    @Column(name = "sms_text", nullable = false)
    private String smsText;

    @Column(name = "sender_header", nullable = false)
    private String senderHeader;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(optional = true)
    @JoinColumn(name = "matched_template_id", nullable = true)
    private RegexTemplate matchedTemplate;

    @Lob
    @Column(name = "extracted_fields", columnDefinition = "TEXT")
    private String extractedFields; // JSON string of extracted fields

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
