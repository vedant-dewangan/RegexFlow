package com.regexflow.backend.Mapper;

import com.regexflow.backend.Dto.BankDto;
import com.regexflow.backend.Entity.Bank;
import com.regexflow.backend.Entity.RegexTemplate;

import java.util.stream.Collectors;

public class BankMapper {

    public static BankDto toDto(Bank bank) {
        if (bank == null) {
            return null;
        }

        BankDto dto = new BankDto();
        dto.setBId(bank.getBId());
        dto.setName(bank.getName());
        dto.setAddress(bank.getAddress());

        if (bank.getRegexTemplates() != null) {
            dto.setRegexTemplateIds(
                bank.getRegexTemplates().stream()
                    .map(RegexTemplate::getTemplateId)
                    .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public static Bank toEntity(BankDto dto) {
        if (dto == null) {
            return null;
        }

        Bank bank = new Bank();
        bank.setBId(dto.getBId());
        bank.setName(dto.getName());
        bank.setAddress(dto.getAddress());

        // Note: regexTemplates list is not set here to avoid circular dependencies
        // It should be set separately when needed

        return bank;
    }
}
