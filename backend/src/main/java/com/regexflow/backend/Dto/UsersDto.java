package com.regexflow.backend.Dto;

import com.regexflow.backend.Enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersDto {
    private Long uId;
    private String name;
    private String email;
    private String passwordHash;
    private UserRole role;
    private List<Long> auditLogIds;
    private List<Long> regexTemplateIds;
}
