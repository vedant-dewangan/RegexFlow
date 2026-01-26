package com.regexflow.backend.Dto;

import com.regexflow.backend.Enums.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long auditId;
    private AuditStatus status;
    private Long verifiedById;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private Long templateId;
}
