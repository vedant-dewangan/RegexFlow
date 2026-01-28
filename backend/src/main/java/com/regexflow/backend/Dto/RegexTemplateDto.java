package com.regexflow.backend.Dto;

import com.regexflow.backend.Enums.PaymentType;
import com.regexflow.backend.Enums.RegexTemplateStatus;
import com.regexflow.backend.Enums.SmsType;
import com.regexflow.backend.Enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegexTemplateDto {
    private Long templateId;
    
    @NotBlank(message = "Sender Header is required and cannot be empty")
    private String senderHeader;
    
    @NotBlank(message = "Regex Pattern is required and cannot be empty")
    private String pattern;
    
    @NotBlank(message = "Sample Raw Message is required and cannot be empty")
    private String sampleRawMsg;
    
    @NotNull(message = "SMS Type is required")
    private SmsType smsType;
    
    @NotNull(message = "Transaction Type is required")
    private TransactionType transactionType;
    
    private RegexTemplateStatus status;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    
    @NotNull(message = "Bank ID is required")
    private Long bankId;
    
    private String bankName;
    private Long auditLogId;
    
    @NotNull(message = "Payment Type is required")
    private PaymentType paymentType;
}
