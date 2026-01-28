package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.RegexTemplateDto;
import com.regexflow.backend.Entity.Bank;
import com.regexflow.backend.Entity.RegexTemplate;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Enums.RegexTemplateStatus;
import com.regexflow.backend.Mapper.RegexTemplateMapper;
import com.regexflow.backend.Repository.BankRepository;
import com.regexflow.backend.Repository.RegexTemplateRepository;
import com.regexflow.backend.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegexTemplateService {
    private final RegexTemplateRepository regexTemplateRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;

    public RegexTemplateService(
            RegexTemplateRepository regexTemplateRepository,
            UserRepository userRepository,
            BankRepository bankRepository) {
        this.regexTemplateRepository = regexTemplateRepository;
        this.userRepository = userRepository;
        this.bankRepository = bankRepository;
    }

    public List<RegexTemplateDto> getAllRegexTemplates() {
        return regexTemplateRepository.findAll()
            .stream()
            .map(RegexTemplateMapper::toDto)
            .toList();
    }

    public List<RegexTemplateDto> getTemplatesByMakerId(Long makerId) {
        Users maker = userRepository.findById(makerId)
            .orElseThrow(() -> new RuntimeException("Maker not found with id: " + makerId));
        
        return regexTemplateRepository.findByCreatedBy(maker)
            .stream()
            .map(RegexTemplateMapper::toDto)
            .toList();
    }

    public RegexTemplateDto saveAsDraft(RegexTemplateDto dto, Long userId) {
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Bank bank = bankRepository.findById(dto.getBankId())
            .orElseThrow(() -> new RuntimeException("Bank not found with id: " + dto.getBankId()));

        // Check for duplicate draft
        Optional<RegexTemplate> existingDraft = regexTemplateRepository.findBySenderHeaderAndPatternAndBankAndSmsTypeAndTransactionTypeAndPaymentTypeAndStatus(
            dto.getSenderHeader(),
            dto.getPattern(),
            bank,
            dto.getSmsType(),
            dto.getTransactionType(),
            dto.getPaymentType(),
            RegexTemplateStatus.DRAFT
        );

        if (existingDraft.isPresent()) {
            throw new RuntimeException("A draft template with the same pattern, sender header, bank, SMS type, transaction type, and payment type already exists");
        }

        RegexTemplate template = createTemplateFromDto(dto, userId);
        template.setStatus(RegexTemplateStatus.DRAFT);
        RegexTemplate savedTemplate = regexTemplateRepository.save(template);
        return RegexTemplateMapper.toDto(savedTemplate);
    }

    public RegexTemplateDto updateToPending(Long templateId, RegexTemplateDto dto, Long userId) {
        RegexTemplate existingTemplate = regexTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

        // Verify the template belongs to the user or user is admin
        if (!existingTemplate.getCreatedBy().getUId().equals(userId)) {
            throw new RuntimeException("You can only update templates created by you");
        }

        // Only allow updating DRAFT templates to PENDING
        if (existingTemplate.getStatus() != RegexTemplateStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT templates can be updated to PENDING status");
        }

        Bank bank = bankRepository.findById(dto.getBankId())
            .orElseThrow(() -> new RuntimeException("Bank not found with id: " + dto.getBankId()));

        // Update template fields
        existingTemplate.setSenderHeader(dto.getSenderHeader());
        existingTemplate.setPattern(dto.getPattern());
        existingTemplate.setSampleRawMsg(dto.getSampleRawMsg());
        existingTemplate.setSmsType(dto.getSmsType());
        existingTemplate.setTransactionType(dto.getTransactionType());
        existingTemplate.setPaymentType(dto.getPaymentType());
        existingTemplate.setBank(bank);
        existingTemplate.setStatus(RegexTemplateStatus.PENDING);

        RegexTemplate updatedTemplate = regexTemplateRepository.save(existingTemplate);
        return RegexTemplateMapper.toDto(updatedTemplate);
    }

    private RegexTemplate createTemplateFromDto(RegexTemplateDto dto, Long userId) {
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Bank bank = bankRepository.findById(dto.getBankId())
            .orElseThrow(() -> new RuntimeException("Bank not found with id: " + dto.getBankId()));

        RegexTemplate template = RegexTemplateMapper.toEntity(dto);
        template.setTemplateId(null); // Ensure it's a new entity
        template.setCreatedBy(user);
        template.setBank(bank);
        
        return template;
    }
}
