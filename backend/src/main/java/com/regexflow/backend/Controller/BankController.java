package com.regexflow.backend.Controller;

import com.regexflow.backend.Dto.BankDto;
import com.regexflow.backend.Enums.UserRole;
import com.regexflow.backend.Service.BankService;
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
@RequestMapping("/bank")
public class BankController {
    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBank(@Valid @RequestBody BankDto bankDto, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try{
            BankDto createdBank = bankService.createBank(bankDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBank);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<BankDto>> getAllBanks(HttpSession session) {
        if (isCustomer(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(bankService.getAllBanks());
    }

    private boolean isAdmin(HttpSession session) {
        return session != null
            && session.getAttribute("token") != null
            && session.getAttribute("userId") != null
            && UserRole.ADMIN.name().equals(String.valueOf(session.getAttribute("userRole")));
    }

    private boolean isCustomer(HttpSession session) {
        return session != null
            && session.getAttribute("token") != null
            && session.getAttribute("userId") != null
            && UserRole.CUSTOMER.name().equals(String.valueOf(session.getAttribute("userRole")));
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
    }
}
