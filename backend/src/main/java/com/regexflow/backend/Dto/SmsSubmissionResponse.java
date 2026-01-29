package com.regexflow.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsSubmissionResponse {
    private Long smsId;
    private String smsText;
    private Boolean hasMatch;
    private Long matchedTemplateId;
    private String matchedTemplateSenderHeader;
    private ExtractedFieldsDto extractedFields;
    private String message; // e.g., "No available template" or "Template matched successfully"
    private java.time.LocalDateTime createdAt;
}
