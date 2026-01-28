package com.regexflow.backend.Dto;

import com.regexflow.backend.Enums.PaymentType;
import com.regexflow.backend.Enums.RegexTemplateStatus;
import com.regexflow.backend.Enums.SmsType;
import com.regexflow.backend.Enums.TransactionType;
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
    private String sampleRawMsg;
    private SmsType smsType;
    private TransactionType transactionType;
    private RegexTemplateStatus status;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long bankId;
    private String bankName;
    private Long auditLogId;
    private PaymentType paymentType;
}
