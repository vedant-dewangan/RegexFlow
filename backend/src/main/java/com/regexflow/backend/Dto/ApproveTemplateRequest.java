package com.regexflow.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveTemplateRequest {
    private Long templateId;
    private String comment; // Optional comment for approval
}
