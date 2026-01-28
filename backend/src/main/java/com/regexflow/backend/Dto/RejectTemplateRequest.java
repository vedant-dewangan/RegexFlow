package com.regexflow.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectTemplateRequest {
    private Long templateId;
    private String reason; // Optional reason for rejection
}
