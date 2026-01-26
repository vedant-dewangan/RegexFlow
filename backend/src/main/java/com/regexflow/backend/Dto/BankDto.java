package com.regexflow.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankDto {
    private Long bId;
    private String name;
    private String address;
    private List<Long> regexTemplateIds;
}
