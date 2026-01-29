package com.regexflow.backend.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regexflow.backend.Dto.*;
import com.regexflow.backend.Entity.RegexTemplate;
import com.regexflow.backend.Entity.Sms;
import com.regexflow.backend.Entity.TemplateRequestNotification;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Enums.NotificationStatus;
import com.regexflow.backend.Enums.RegexTemplateStatus;
import com.regexflow.backend.Repository.RegexTemplateRepository;
import com.regexflow.backend.Repository.SmsRepository;
import com.regexflow.backend.Repository.TemplateRequestNotificationRepository;
import com.regexflow.backend.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SmsService {
    private final SmsRepository smsRepository;
    private final RegexTemplateRepository regexTemplateRepository;
    private final TemplateRequestNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RegexProcessService regexProcessService;
    private final ObjectMapper objectMapper;

    public SmsService(
            SmsRepository smsRepository,
            RegexTemplateRepository regexTemplateRepository,
            TemplateRequestNotificationRepository notificationRepository,
            UserRepository userRepository,
            RegexProcessService regexProcessService,
            ObjectMapper objectMapper) {
        this.smsRepository = smsRepository;
        this.regexTemplateRepository = regexTemplateRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.regexProcessService = regexProcessService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SmsSubmissionResponse processSms(String smsText, Long userId) {
        // Extract sender header (text before first colon)
        String senderHeader = extractSenderHeader(smsText);
        
        // Find user
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Find all VERIFIED templates with matching sender header
        List<RegexTemplate> templates = regexTemplateRepository.findBySenderHeaderAndStatus(
            senderHeader, RegexTemplateStatus.VERIFIED);
        
        Sms sms = new Sms();
        sms.setSmsText(smsText);
        sms.setSenderHeader(senderHeader);
        sms.setUser(user);
        
        SmsSubmissionResponse response = new SmsSubmissionResponse();
        
        if (templates.isEmpty()) {
            // No templates found - store SMS without template and create notification
            Sms savedSms = smsRepository.save(sms);
            
            // Create notification for makers
            TemplateRequestNotification notification = new TemplateRequestNotification();
            notification.setSms(savedSms);
            notification.setSmsText(smsText);
            notification.setSenderHeader(senderHeader);
            notification.setRequestedBy(user);
            notification.setStatus(NotificationStatus.PENDING);
            notificationRepository.save(notification);
            
            response.setSmsId(savedSms.getSmsId());
            response.setSmsText(savedSms.getSmsText());
            response.setHasMatch(false);
            response.setMessage("No available template for sender: " + senderHeader + ". Maker has been notified.");
            response.setCreatedAt(savedSms.getCreatedAt());
            return response;
        }
        
        // Try each template pattern against SMS and find best match
        RegexTemplate bestTemplate = null;
        RegexProcessResponse bestMatchResponse = null;
        int maxFieldsCount = 0;
        
        for (RegexTemplate template : templates) {
            try {
                RegexProcessRequest request = new RegexProcessRequest();
                request.setRegexPattern(template.getPattern());
                request.setRawMsg(smsText);
                request.setSmsType(template.getSmsType());
                request.setPaymentType(template.getPaymentType());
                request.setTransactionType(template.getTransactionType());
                
                RegexProcessResponse matchResponse = regexProcessService.processRegex(request);
                
                // Count non-null extracted fields
                int fieldsCount = countExtractedFields(matchResponse);
                
                if (fieldsCount > maxFieldsCount) {
                    maxFieldsCount = fieldsCount;
                    bestTemplate = template;
                    bestMatchResponse = matchResponse;
                }
            } catch (Exception e) {
                // Skip this template if pattern matching fails
                continue;
            }
        }
        
        if (bestTemplate == null || maxFieldsCount == 0) {
            // No template matched - store SMS without template and create notification
            Sms savedSms = smsRepository.save(sms);
            
            // Create notification for makers
            TemplateRequestNotification notification = new TemplateRequestNotification();
            notification.setSms(savedSms);
            notification.setSmsText(smsText);
            notification.setSenderHeader(senderHeader);
            notification.setRequestedBy(user);
            notification.setStatus(NotificationStatus.PENDING);
            notificationRepository.save(notification);
            
            response.setSmsId(savedSms.getSmsId());
            response.setSmsText(savedSms.getSmsText());
            response.setHasMatch(false);
            response.setMessage("No template matched the SMS pattern. Maker has been notified.");
            response.setCreatedAt(savedSms.getCreatedAt());
            return response;
        }
        
        // Store SMS with matched template
        sms.setMatchedTemplate(bestTemplate);
        
        // Convert RegexProcessResponse to ExtractedFieldsDto
        ExtractedFieldsDto extractedFields = convertToExtractedFieldsDto(bestMatchResponse, smsText, bestTemplate.getSmsType());
        
        // Store extracted fields as JSON
        try {
            String extractedFieldsJson = objectMapper.writeValueAsString(extractedFields.getFields());
            sms.setExtractedFields(extractedFieldsJson);
        } catch (JsonProcessingException e) {
            // If JSON conversion fails, store empty JSON
            sms.setExtractedFields("{}");
        }
        
        Sms savedSms = smsRepository.save(sms);
        
        response.setSmsId(savedSms.getSmsId());
        response.setSmsText(savedSms.getSmsText());
        response.setHasMatch(true);
        response.setMatchedTemplateId(bestTemplate.getTemplateId());
        response.setMatchedTemplateSenderHeader(bestTemplate.getSenderHeader());
        response.setExtractedFields(extractedFields);
        response.setMessage("Template matched successfully");
        response.setCreatedAt(savedSms.getCreatedAt());
        
        return response;
    }
    
    public List<SmsSubmissionResponse> getSmsHistory(Long userId) {
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        List<Sms> smsList = smsRepository.findByUserOrderByCreatedAtDesc(user);
        
        return smsList.stream().map(sms -> {
            SmsSubmissionResponse response = new SmsSubmissionResponse();
            response.setSmsId(sms.getSmsId());
            response.setSmsText(sms.getSmsText());
            response.setHasMatch(sms.getMatchedTemplate() != null);
            response.setCreatedAt(sms.getCreatedAt());
            
            if (sms.getMatchedTemplate() != null) {
                response.setMatchedTemplateId(sms.getMatchedTemplate().getTemplateId());
                response.setMatchedTemplateSenderHeader(sms.getMatchedTemplate().getSenderHeader());
                
                // Parse extracted fields from JSON
                if (sms.getExtractedFields() != null && !sms.getExtractedFields().isEmpty()) {
                    try {
                        Map<String, String> fieldsMap = objectMapper.readValue(
                            sms.getExtractedFields(), 
                            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class)
                        );
                        ExtractedFieldsDto extractedFields = new ExtractedFieldsDto();
                        extractedFields.setFields(fieldsMap);
                        extractedFields.setAmount(fieldsMap.get("amount"));
                        extractedFields.setDate(fieldsMap.get("date"));
                        extractedFields.setMerchant(fieldsMap.get("merchant"));
                        extractedFields.setBalance(fieldsMap.get("balance"));
                        
                        // Set smsType from the matched template (most reliable)
                        if (sms.getMatchedTemplate().getSmsType() != null) {
                            extractedFields.setSmsType(sms.getMatchedTemplate().getSmsType().name());
                            extractedFields.setTransactionType(sms.getMatchedTemplate().getSmsType().name());
                        }
                        
                        response.setExtractedFields(extractedFields);
                    } catch (JsonProcessingException e) {
                        // If parsing fails, set empty extracted fields
                        response.setExtractedFields(new ExtractedFieldsDto());
                    }
                }
            }
            
            return response;
        }).collect(Collectors.toList());
    }
    
    private String extractSenderHeader(String smsText) {
        if (smsText == null || smsText.isEmpty()) {
            return "";
        }
        
        int colonIndex = smsText.indexOf(':');
        if (colonIndex > 0) {
            return smsText.substring(0, colonIndex).trim();
        }
        
        // If no colon found, return first word or empty string
        String[] words = smsText.trim().split("\\s+");
        return words.length > 0 ? words[0] : "";
    }
    
    private int countExtractedFields(RegexProcessResponse response) {
        int count = 0;
        try {
            Field[] fields = RegexProcessResponse.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(response);
                if (value instanceof FieldResult) {
                    FieldResult fieldResult = (FieldResult) value;
                    if (fieldResult.getValue() != null && fieldResult.getIndex() >= 0) {
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            // If reflection fails, return 0
            return 0;
        }
        return count;
    }
    
    public List<TemplateRequestNotificationDto> getPendingNotifications() {
        List<TemplateRequestNotification> notifications = notificationRepository
            .findByStatusOrderByCreatedAtDesc(NotificationStatus.PENDING);
        
        return notifications.stream().map(notification -> {
            TemplateRequestNotificationDto dto = new TemplateRequestNotificationDto();
            dto.setNotificationId(notification.getNotificationId());
            dto.setSmsId(notification.getSms().getSmsId());
            dto.setSmsText(notification.getSmsText());
            dto.setSenderHeader(notification.getSenderHeader());
            dto.setRequestedById(notification.getRequestedBy().getUId());
            dto.setRequestedByName(notification.getRequestedBy().getName());
            dto.setStatus(notification.getStatus());
            dto.setCreatedAt(notification.getCreatedAt());
            dto.setResolvedAt(notification.getResolvedAt());
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Transactional
    public void markNotificationAsResolved(Long notificationId) {
        TemplateRequestNotification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        
        notification.setStatus(NotificationStatus.RESOLVED);
        notification.setResolvedAt(java.time.LocalDateTime.now());
        notificationRepository.save(notification);
    }
    
    private ExtractedFieldsDto convertToExtractedFieldsDto(RegexProcessResponse response, String smsText, com.regexflow.backend.Enums.SmsType templateSmsType) {
        ExtractedFieldsDto dto = new ExtractedFieldsDto();
        Map<String, String> fieldsMap = new HashMap<>();
        
        try {
            Field[] fields = RegexProcessResponse.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(response);
                if (value instanceof FieldResult) {
                    FieldResult fieldResult = (FieldResult) value;
                    if (fieldResult.getValue() != null && fieldResult.getIndex() >= 0) {
                        String fieldName = field.getName();
                        fieldsMap.put(fieldName, fieldResult.getValue());
                        
                        // Set common fields for easy access
                        if ("amount".equals(fieldName)) {
                            dto.setAmount(fieldResult.getValue());
                        } else if ("date".equals(fieldName)) {
                            dto.setDate(fieldResult.getValue());
                        } else if ("merchant".equals(fieldName)) {
                            dto.setMerchant(fieldResult.getValue());
                        } else if ("balance".equals(fieldName)) {
                            dto.setBalance(fieldResult.getValue());
                        }
                    }
                }
            }
            
            // Determine transaction type (DEBIT/CREDIT) - prioritize template's smsType
            String transactionType = null;
            
            // First, use the template's smsType if available (most reliable)
            if (templateSmsType != null) {
                transactionType = templateSmsType.name(); // DEBIT or CREDIT
            }
            // Fallback: Check for amountNegative field (indicates debit)
            else if (fieldsMap.containsKey("amountNegative") && fieldsMap.get("amountNegative") != null) {
                transactionType = "DEBIT";
            } 
            // Fallback: Check SMS text for keywords (case-insensitive)
            else if (smsText != null && !smsText.isEmpty()) {
                String smsTextLower = smsText.toLowerCase();
                
                // Common debit keywords
                if (smsTextLower.contains("debited") || 
                    smsTextLower.contains("withdrawn") || 
                    smsTextLower.contains("spent") ||
                    smsTextLower.contains("paid") ||
                    smsTextLower.contains("deducted") ||
                    smsTextLower.contains("debit")) {
                    transactionType = "DEBIT";
                }
                // Common credit keywords
                else if (smsTextLower.contains("credited") || 
                         smsTextLower.contains("received") || 
                         smsTextLower.contains("deposited") ||
                         smsTextLower.contains("added") ||
                         smsTextLower.contains("credit")) {
                    transactionType = "CREDIT";
                }
            }
            
            dto.setTransactionType(transactionType);
            dto.setSmsType(templateSmsType != null ? templateSmsType.name() : transactionType);
            
        } catch (Exception e) {
            // If conversion fails, return empty DTO
        }
        
        dto.setFields(fieldsMap);
        return dto;
    }
}
