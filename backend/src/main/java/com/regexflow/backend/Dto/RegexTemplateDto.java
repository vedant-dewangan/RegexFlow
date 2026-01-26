package com.regexflow.backend.Dto;

import com.regexflow.backend.Enums.RegexTemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegexTemplateDto {
    private Long templateId;
    private String senderHeader;
    private String pattern;
    private String smsType;
    private RegexTemplateStatus status;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long bankId;
    private String bankName;
    private Long auditLogId;
}
