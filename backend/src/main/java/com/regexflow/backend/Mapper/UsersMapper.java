package com.regexflow.backend.Mapper;

import com.regexflow.backend.Dto.UsersDto;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Entity.AuditLog;
import com.regexflow.backend.Entity.RegexTemplate;

import java.util.stream.Collectors;

public class UsersMapper {

    public static UsersDto toDto(Users user) {
        if (user == null) {
            return null;
        }

        UsersDto dto = new UsersDto();
        dto.setUId(user.getUId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPasswordHash(user.getPasswordHash());
        dto.setRole(user.getRole());

        if (user.getAuditLogs() != null) {
            dto.setAuditLogIds(
                user.getAuditLogs().stream()
                    .map(AuditLog::getAuditId)
                    .collect(Collectors.toList())
            );
        }

        if (user.getRegexTemplates() != null) {
            dto.setRegexTemplateIds(
                user.getRegexTemplates().stream()
                    .map(RegexTemplate::getTemplateId)
                    .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public static Users toEntity(UsersDto dto) {
        if (dto == null) {
            return null;
        }

        Users user = new Users();
        user.setUId(dto.getUId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPasswordHash());
        user.setRole(dto.getRole());

        // Note: auditLogs and regexTemplates lists are not set here to avoid circular dependencies
        // They should be set separately when needed

        return user;
    }
}
