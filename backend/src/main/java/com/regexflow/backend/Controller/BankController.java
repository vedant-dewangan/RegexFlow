package com.regexflow.backend.Controller;

import com.regexflow.backend.Dto.BankDto;
import com.regexflow.backend.Enums.UserRole;
import com.regexflow.backend.Service.BankService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bank")
public class BankController {
    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping("/create")
    public ResponseEntity<BankDto> createBank(@RequestBody BankDto bankDto, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        BankDto createdBank = bankService.createBank(bankDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBank);
    }

    @GetMapping
    public ResponseEntity<List<BankDto>> getAllBanks(HttpSession session) {
        if (!isAdmin(session)) {
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
}
