package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.RegexTemplateDto;
import com.regexflow.backend.Entity.AuditLog;
import com.regexflow.backend.Entity.RegexTemplate;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Enums.AuditStatus;
import com.regexflow.backend.Enums.RegexTemplateStatus;
import com.regexflow.backend.Mapper.RegexTemplateMapper;
import com.regexflow.backend.Repository.AuditLogRepository;
import com.regexflow.backend.Repository.RegexTemplateRepository;
import com.regexflow.backend.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CheckerService {
    private final RegexTemplateRepository regexTemplateRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public CheckerService(
            RegexTemplateRepository regexTemplateRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository) {
        this.regexTemplateRepository = regexTemplateRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Get all templates with PENDING status that need review
     */
    public List<RegexTemplateDto> getPendingTemplates() {
        return regexTemplateRepository.findByStatus(RegexTemplateStatus.PENDING)
            .stream()
            .map(RegexTemplateMapper::toDto)
            .toList();
    }

    /**
     * Approve a template - changes status from PENDING to VERIFIED
     * and creates an audit log with APPROVED status
     */
    @Transactional
    public RegexTemplateDto approveTemplate(Long templateId, Long checkerId) {
        // Fetch the template
        RegexTemplate template = regexTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

        // Verify the template is in PENDING status
        if (template.getStatus() != RegexTemplateStatus.PENDING) {
            throw new RuntimeException("Only PENDING templates can be approved. Current status: " + template.getStatus());
        }

        // Fetch the checker user
        Users checker = userRepository.findById(checkerId)
            .orElseThrow(() -> new RuntimeException("Checker not found with id: " + checkerId));

        // Update template status to VERIFIED
        template.setStatus(RegexTemplateStatus.VERIFIED);
        RegexTemplate savedTemplate = regexTemplateRepository.save(template);

        // Check if audit log already exists for this template
        Optional<AuditLog> existingAuditLogOpt = auditLogRepository.findByTemplate(savedTemplate);
        
        AuditLog auditLog;
        if (existingAuditLogOpt.isPresent()) {
            // Update existing audit log
            auditLog = existingAuditLogOpt.get();
            auditLog.setStatus(AuditStatus.APPROVED);
            auditLog.setVerifiedBy(checker);
            auditLog.setVerifiedAt(LocalDateTime.now());
        } else {
            // Create new audit log for approval
            auditLog = new AuditLog();
            auditLog.setTemplate(savedTemplate);
            auditLog.setVerifiedBy(checker);
            auditLog.setStatus(AuditStatus.APPROVED);
        }
        auditLogRepository.save(auditLog);

        return RegexTemplateMapper.toDto(savedTemplate);
    }

    /**
     * Reject a template - changes status from PENDING back to DRAFT
     * and creates an audit log with REJECTED status
     */
    @Transactional
    public RegexTemplateDto rejectTemplate(Long templateId, Long checkerId) {
        // Fetch the template
        RegexTemplate template = regexTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

        // Verify the template is in PENDING status
        if (template.getStatus() != RegexTemplateStatus.PENDING) {
            throw new RuntimeException("Only PENDING templates can be rejected. Current status: " + template.getStatus());
        }

        // Fetch the checker user
        Users checker = userRepository.findById(checkerId)
            .orElseThrow(() -> new RuntimeException("Checker not found with id: " + checkerId));

        // Update template status back to DRAFT so maker can revise it
        template.setStatus(RegexTemplateStatus.DRAFT);
        RegexTemplate savedTemplate = regexTemplateRepository.save(template);

        // Check if audit log already exists for this template
        Optional<AuditLog> existingAuditLogOpt = auditLogRepository.findByTemplate(savedTemplate);
        
        AuditLog auditLog;
        if (existingAuditLogOpt.isPresent()) {
            // Update existing audit log
            auditLog = existingAuditLogOpt.get();
            auditLog.setStatus(AuditStatus.REJECTED);
            auditLog.setVerifiedBy(checker);
            auditLog.setVerifiedAt(LocalDateTime.now());
        } else {
            // Create new audit log for rejection
            auditLog = new AuditLog();
            auditLog.setTemplate(savedTemplate);
            auditLog.setVerifiedBy(checker);
            auditLog.setStatus(AuditStatus.REJECTED);
        }
        auditLogRepository.save(auditLog);

        return RegexTemplateMapper.toDto(savedTemplate);
    }

    /**
     * Get all verified templates
     */
    public List<RegexTemplateDto> getVerifiedTemplates() {
        return regexTemplateRepository.findByStatus(RegexTemplateStatus.VERIFIED)
            .stream()
            .map(RegexTemplateMapper::toDto)
            .toList();
    }

    /**
     * Get a specific template by ID (for review details)
     */
    public RegexTemplateDto getTemplateById(Long templateId) {
        RegexTemplate template = regexTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));
        return RegexTemplateMapper.toDto(template);
    }
}
