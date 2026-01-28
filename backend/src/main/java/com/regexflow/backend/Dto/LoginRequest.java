package com.regexflow.backend.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email is required and cannot be empty")
    @Email(message = "Email must be a valid email address")
    private String email;
    
    @NotBlank(message = "Password is required and cannot be empty")
    private String password;
}
