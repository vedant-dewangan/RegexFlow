package com.regexflow.backend.Dto;

import com.regexflow.backend.Entity.AuditLog;
import com.regexflow.backend.Entity.RegexTemplate;
import com.regexflow.backend.Enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private String name;
    private String email;
    private UserRole role;
}
