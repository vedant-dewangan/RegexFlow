package com.regexflow.backend.Controller;

import com.regexflow.backend.Dto.RegexTemplateDto;
import com.regexflow.backend.Enums.UserRole;
import com.regexflow.backend.Service.CheckerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/checker")
public class CheckerController {

    @Autowired
    private CheckerService checkerService;

    /**
     * GET /checker/pending
     * Get all templates with PENDING status that need review
     * Only CHECKER and ADMIN roles can access this endpoint
     */
    @GetMapping("/pending")
    public ResponseEntity<List<RegexTemplateDto>> getPendingTemplates(HttpSession session) {
        if (!isCheckerOrAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            List<RegexTemplateDto> pendingTemplates = checkerService.getPendingTemplates();
            return ResponseEntity.ok(pendingTemplates);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /checker/verified
     * Get all templates with VERIFIED status
     * Only CHECKER and ADMIN roles can access this endpoint
     */
    @GetMapping("/verified")
    public ResponseEntity<List<RegexTemplateDto>> getVerifiedTemplates(HttpSession session) {
        if (!isCheckerOrAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            List<RegexTemplateDto> verifiedTemplates = checkerService.getVerifiedTemplates();
            return ResponseEntity.ok(verifiedTemplates);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /checker/template/{templateId}
     * Get a specific template by ID for detailed review
     * Only CHECKER and ADMIN roles can access this endpoint
     */
//    @GetMapping("/template/{templateId}")
//    public ResponseEntity<?> getTemplateById(
//            @PathVariable Long templateId,
//            HttpSession session) {
//        if (!isCheckerOrAdmin(session)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        try {
//            RegexTemplateDto template = checkerService.getTemplateById(templateId);
//            return ResponseEntity.ok(template);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body("{\"error\": \"" + e.getMessage() + "\"}");
//        }
//    }

    /**
     * PUT /checker/approve/{templateId}
     * Approve a template - changes status from PENDING to VERIFIED
     * Only CHECKER and ADMIN roles can approve templates
     */
    @PutMapping("/approve/{templateId}")
    public ResponseEntity<?> approveTemplate(
            @PathVariable Long templateId,
            HttpSession session) {
        if (!isCheckerOrAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long checkerId = (Long) session.getAttribute("userId");
            if (checkerId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"User ID not found in session\"}");
            }
            RegexTemplateDto approvedTemplate = checkerService.approveTemplate(templateId, checkerId);
            return ResponseEntity.ok(approvedTemplate);
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Unknown error";
            if (errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"" + errorMessage + "\"}");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + errorMessage + "\"}");
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Unknown error occurred";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"" + errorMessage + "\"}");
        }
    }

    /**
     * PUT /checker/reject/{templateId}
     * Reject a template - changes status from PENDING back to DRAFT
     * Only CHECKER and ADMIN roles can reject templates
     */
    @PutMapping("/reject/{templateId}")
    public ResponseEntity<?> rejectTemplate(
            @PathVariable Long templateId,
            HttpSession session) {
        if (!isCheckerOrAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long checkerId = (Long) session.getAttribute("userId");
            if (checkerId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"User ID not found in session\"}");
            }
            RegexTemplateDto rejectedTemplate = checkerService.rejectTemplate(templateId, checkerId);
            return ResponseEntity.ok(rejectedTemplate);
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Unknown error";
            if (errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"" + errorMessage + "\"}");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + errorMessage + "\"}");
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Unknown error occurred";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"" + errorMessage + "\"}");
        }
    }

    /**
     * Helper method to check if user is CHECKER or ADMIN
     */
    private boolean isCheckerOrAdmin(HttpSession session) {
        if (session == null || session.getAttribute("token") == null || session.getAttribute("userId") == null) {
            return false;
        }
        String userRole = String.valueOf(session.getAttribute("userRole"));
        return UserRole.CHECKER.name().equals(userRole) || UserRole.ADMIN.name().equals(userRole);
    }
}
