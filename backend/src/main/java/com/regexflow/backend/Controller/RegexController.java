package com.regexflow.backend.Controller;

import com.regexflow.backend.Dto.RegexProcessRequest;
import com.regexflow.backend.Dto.RegexProcessResponse;
import com.regexflow.backend.Dto.RegexTemplateDto;
import com.regexflow.backend.Enums.UserRole;
import com.regexflow.backend.Service.RegexProcessService;
import com.regexflow.backend.Service.RegexTemplateService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/regex")
public class RegexController {

    @Autowired
    private RegexProcessService regexProcessService;

    @Autowired
    private RegexTemplateService regexTemplateService;

    @GetMapping
    public ResponseEntity<List<RegexTemplateDto>> getAllRegexTemplates(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(regexTemplateService.getAllRegexTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<RegexTemplateDto>> getTemplatesByMakerId(
            @PathVariable Long id,
            HttpSession session) {
        if (!isAdminMakerChecker(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(regexTemplateService.getTemplatesByMakerId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/save-as-draft")
    public ResponseEntity<?> saveAsDraft(
            @RequestBody RegexTemplateDto dto,
            HttpSession session) {
        if (!isMaker(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Long userId = (Long) session.getAttribute("userId");
            RegexTemplateDto savedTemplate = regexTemplateService.saveAsDraft(dto, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTemplate);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/push/{templateId}")
    public ResponseEntity<?> pushTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody RegexTemplateDto dto,
            HttpSession session) {
        if (!isMaker(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Long userId = (Long) session.getAttribute("userId");
            RegexTemplateDto updatedTemplate = regexTemplateService.updateToPending(templateId, dto, userId);
            return ResponseEntity.ok(updatedTemplate);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/process")
    public ResponseEntity<?> processRegex(
            @RequestBody RegexProcessRequest request,
            HttpSession session) {
        if (!isAdminMakerChecker(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (request.getRegexPattern() == null || request.getRegexPattern().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"Regex pattern is required\"}");
        }

        if (request.getRawMsg() == null || request.getRawMsg().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"Raw message is required\"}");
        }

        if (request.getSmsType() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"SMS type is required\"}");
        }

        if (request.getPaymentType() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"Payment type is required\"}");
        }

        try {
            RegexProcessResponse response = regexProcessService.processRegex(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private boolean isAdmin(HttpSession session) {
        if (session == null || session.getAttribute("token") == null || session.getAttribute("userId") == null) {
            return false;
        }
        String userRole = String.valueOf(session.getAttribute("userRole"));
        return UserRole.ADMIN.name().equals(userRole);
    }

    private boolean isMaker(HttpSession session) {
        if (session == null || session.getAttribute("token") == null || session.getAttribute("userId") == null) {
            return false;
        }
        String userRole = String.valueOf(session.getAttribute("userRole"));
        return UserRole.MAKER.name().equals(userRole);
    }

    private boolean isAdminMakerChecker(HttpSession session) {
        if (session == null || session.getAttribute("token") == null || session.getAttribute("userId") == null) {
            return false;
        }
        String userRole = String.valueOf(session.getAttribute("userRole"));
        return UserRole.ADMIN.name().equals(userRole)
            || UserRole.MAKER.name().equals(userRole)
            || UserRole.CHECKER.name().equals(userRole);
    }

    /**
     * Exception handler for validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String message = error.getDefaultMessage();
            if (errorMessage.length() > 0) {
                errorMessage.append(", ");
            }
            errorMessage.append(message);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("{\"error\": \"" + errorMessage.toString() + "\"}");
    }
}
