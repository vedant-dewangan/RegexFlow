package com.regexflow.backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sms")
@Getter
@Setter
public class Sms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sms_id", nullable = false)
    private Long smsId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Lob
    @Column(name = "raw_sms", nullable = false)
    private String rawSms;
}
