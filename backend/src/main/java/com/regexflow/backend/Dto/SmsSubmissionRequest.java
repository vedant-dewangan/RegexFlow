package com.regexflow.backend.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsSubmissionRequest {
    @NotBlank(message = "SMS text is required")
    private String smsText;
}
