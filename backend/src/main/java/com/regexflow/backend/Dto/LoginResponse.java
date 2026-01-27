package com.regexflow.backend.Dto;

import com.regexflow.backend.Enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String token;
    private String sessionId;
    private UserResponseDto user;
}
