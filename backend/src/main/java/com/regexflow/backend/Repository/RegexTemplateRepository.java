package com.regexflow.backend.Repository;

import com.regexflow.backend.Entity.Bank;
import com.regexflow.backend.Entity.RegexTemplate;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Enums.PaymentType;
import com.regexflow.backend.Enums.RegexTemplateStatus;
import com.regexflow.backend.Enums.SmsType;
import com.regexflow.backend.Enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegexTemplateRepository extends JpaRepository<RegexTemplate,Long> {
    List<RegexTemplate> findByCreatedBy(Users createdBy);
    
    List<RegexTemplate> findByStatus(RegexTemplateStatus status);
    
    Optional<RegexTemplate> findBySenderHeaderAndPatternAndBankAndSmsTypeAndTransactionTypeAndPaymentTypeAndStatus(
        String senderHeader,
        String pattern,
        Bank bank,
        SmsType smsType,
        TransactionType transactionType,
        PaymentType paymentType,
        RegexTemplateStatus status
    );
    
    List<RegexTemplate> findBySenderHeaderAndStatus(String senderHeader, RegexTemplateStatus status);
}
