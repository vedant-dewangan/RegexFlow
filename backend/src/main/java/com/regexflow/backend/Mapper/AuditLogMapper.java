package com.regexflow.backend.Mapper;

import com.regexflow.backend.Dto.AuditLogDto;
import com.regexflow.backend.Entity.AuditLog;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Entity.RegexTemplate;

public class AuditLogMapper {

    public static AuditLogDto toDto(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        AuditLogDto dto = new AuditLogDto();
        dto.setAuditId(auditLog.getAuditId());
        dto.setStatus(auditLog.getStatus());
        if (auditLog.getVerifiedBy() != null) {
            dto.setVerifiedById(auditLog.getVerifiedBy().getUId());
            dto.setVerifiedByName(auditLog.getVerifiedBy().getName());
        }

        if (auditLog.getTemplate() != null) {
            dto.setTemplateId(auditLog.getTemplate().getTemplateId());
        }

        return dto;
    }

    public static AuditLog toEntity(AuditLogDto dto) {
        if (dto == null) {
            return null;
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setAuditId(dto.getAuditId());
        auditLog.setStatus(dto.getStatus());
        auditLog.setVerifiedAt(dto.getVerifiedAt());

        if (dto.getVerifiedById() != null) {
            Users user = new Users();
            user.setUId(dto.getVerifiedById());
            auditLog.setVerifiedBy(user);
        }

        if (dto.getTemplateId() != null) {
            RegexTemplate template = new RegexTemplate();
            template.setTemplateId(dto.getTemplateId());
            auditLog.setTemplate(template);
        }

        return auditLog;
    }
}
