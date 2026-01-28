package com.regexflow.backend.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Name is required and cannot be empty")
    private String name;
    
    @NotBlank(message = "Email is required and cannot be empty")
    @Email(message = "Email must be a valid email address")
    private String email;
    
    @NotBlank(message = "Password is required and cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
    // Role will default to CUSTOMER if not provided
}
