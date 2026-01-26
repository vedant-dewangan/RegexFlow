package com.regexflow.backend.Mapper;

import com.regexflow.backend.Dto.RegexTemplateDto;
import com.regexflow.backend.Entity.RegexTemplate;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Entity.Bank;
import com.regexflow.backend.Entity.AuditLog;

public class RegexTemplateMapper {

    public static RegexTemplateDto toDto(RegexTemplate template) {
        if (template == null) {
            return null;
        }

        RegexTemplateDto dto = new RegexTemplateDto();
        dto.setTemplateId(template.getTemplateId());
        dto.setSenderHeader(template.getSenderHeader());
        dto.setPattern(template.getPattern());
        dto.setSmsType(template.getSmsType());
        dto.setStatus(template.getStatus());
        dto.setPaymentType(template.getPaymentType());

        if (template.getCreatedBy() != null) {
            dto.setCreatedById(template.getCreatedBy().getUId());
            dto.setCreatedByName(template.getCreatedBy().getName());
        }

        if (template.getBank() != null) {
            dto.setBankId(template.getBank().getBId());
            dto.setBankName(template.getBank().getName());
        }

        if (template.getAuditLog() != null) {
            dto.setAuditLogId(template.getAuditLog().getAuditId());
        }

        return dto;
    }

    public static RegexTemplate toEntity(RegexTemplateDto dto) {
        if (dto == null) {
            return null;
        }

        RegexTemplate template = new RegexTemplate();
        template.setTemplateId(dto.getTemplateId());
        template.setSenderHeader(dto.getSenderHeader());
        template.setPattern(dto.getPattern());
        template.setSmsType(dto.getSmsType());
        template.setStatus(dto.getStatus());
        template.setCreatedAt(dto.getCreatedAt());
        template.setPaymentType(dto.getPaymentType());

        if (dto.getCreatedById() != null) {
            Users user = new Users();
            user.setUId(dto.getCreatedById());
            template.setCreatedBy(user);
        }

        if (dto.getBankId() != null) {
            Bank bank = new Bank();
            bank.setBId(dto.getBankId());
            template.setBank(bank);
        }

        // Note: auditLog is not set here to avoid circular dependencies
        // It should be set separately when needed

        return template;
    }
}
