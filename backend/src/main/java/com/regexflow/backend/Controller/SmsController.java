package com.regexflow.backend.Controller;

import com.regexflow.backend.Dto.SmsSubmissionRequest;
import com.regexflow.backend.Dto.SmsSubmissionResponse;
import com.regexflow.backend.Dto.TemplateRequestNotificationDto;
import com.regexflow.backend.Enums.UserRole;
import com.regexflow.backend.Service.SmsService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sms")
public class SmsController {
    
    @Autowired
    private SmsService smsService;
    
    @PostMapping("/submit")
    public ResponseEntity<?> submitSms(
            @Valid @RequestBody SmsSubmissionRequest request,
            HttpSession session) {
        if (!isCustomer(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            SmsSubmissionResponse response = smsService.processSms(request.getSmsText(), userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<SmsSubmissionResponse>> getSmsHistory(HttpSession session) {
        if (!isCustomer(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<SmsSubmissionResponse> history = smsService.getSmsHistory(userId);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/notifications/pending")
    public ResponseEntity<List<TemplateRequestNotificationDto>> getPendingNotifications(HttpSession session) {
        if (!isMakerOrAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            List<TemplateRequestNotificationDto> notifications = smsService.getPendingNotifications();
            return ResponseEntity.ok(notifications);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PutMapping("/notifications/{notificationId}/resolve")
    public ResponseEntity<?> resolveNotification(
            @PathVariable Long notificationId,
            HttpSession session) {
        if (!isMakerOrAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            smsService.markNotificationAsResolved(notificationId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    private boolean isCustomer(HttpSession session) {
        if (session == null || session.getAttribute("token") == null || session.getAttribute("userId") == null) {
            return false;
        }
        String userRole = String.valueOf(session.getAttribute("userRole"));
        return UserRole.CUSTOMER.name().equals(userRole);
    }
    
    private boolean isMakerOrAdmin(HttpSession session) {
        if (session == null || session.getAttribute("token") == null || session.getAttribute("userId") == null) {
            return false;
        }
        String userRole = String.valueOf(session.getAttribute("userRole"));
        return UserRole.MAKER.name().equals(userRole) || UserRole.ADMIN.name().equals(userRole);
    }
}
