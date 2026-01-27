package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.BankDto;
import com.regexflow.backend.Entity.Bank;
import com.regexflow.backend.Mapper.BankMapper;
import com.regexflow.backend.Repository.BankRepository;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class BankService {
    private final BankRepository bankRepository;

    public BankService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    public BankDto createBank(BankDto bankDto) {
        if(bankRepository.existsByAddress(bankDto.getAddress()) && bankRepository.existsByName(bankDto.getName())){
            throw new RuntimeException("Bank already exists");
        }
        Bank bank = BankMapper.toEntity(bankDto);
        bank.setBId(null);
        Bank savedBank = bankRepository.save(bank);
        return BankMapper.toDto(savedBank);
    }

    public List<BankDto> getAllBanks() {
        return bankRepository.findAll()
            .stream()
            .map(BankMapper::toDto)
            .toList();
    }
}
