package com.regexflow.backend.Dto;

import com.regexflow.backend.Enums.PaymentType;
import com.regexflow.backend.Enums.SmsType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegexProcessRequest {
    private String bankAddress;
    private SmsType smsType;
    private PaymentType paymentType;
    private String regexPattern;
    private String rawMsg;
    private String bankName;
    private String transactionType;
}
