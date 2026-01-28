package com.regexflow.backend.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankDto {
    private Long bId;
    
    @NotBlank(message = "Bank name is required and cannot be empty")
    private String name;
    
    @NotBlank(message = "Bank address is required and cannot be empty")
    private String address;
    
    private List<Long> regexTemplateIds;
}
