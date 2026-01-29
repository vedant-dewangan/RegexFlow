package com.regexflow.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedFieldsDto {
    // Simplified DTO for extracted fields - using Map for flexibility
    private Map<String, String> fields; // fieldName -> value mapping
    
    // Common fields for easy access
    private String amount;
    private String date;
    private String merchant;
    private String balance;
    private String transactionType; // DEBIT or CREDIT
    private String smsType; // DEBIT or CREDIT from template
}
