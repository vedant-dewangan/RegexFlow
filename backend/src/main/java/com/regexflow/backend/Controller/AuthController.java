package com.regexflow.backend.Controller;

import com.regexflow.backend.Dto.LoginRequest;
import com.regexflow.backend.Dto.LoginResponse;
import com.regexflow.backend.Dto.RegisterRequest;
import com.regexflow.backend.Service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authentication endpoints
 * Base path: /auth (all endpoints start with /auth)
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * ENDPOINT: POST /auth/login
     * @PostMapping("/login") = This handles POST requests to /auth/login
     * @RequestBody LoginRequest = Expects JSON: {"email": "...", "password": "..."}
     * 
     * Example request:
     * POST http://localhost:8080/auth/login
     * Body: {"email": "user@example.com", "password": "mypassword"}
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,  // Gets email & password from JSON body
            HttpSession session) {  // Spring automatically provides the session
        try {
            LoginResponse response = authService.login(request, session);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setMessage("Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * ENDPOINT: POST /auth/register
     * @PostMapping("/register") = This handles POST requests to /auth/register
     * 
     * Example request:
     * POST http://localhost:8080/auth/register
     * Body: {"name": "John Doe", "email": "john@example.com", "password": "pass123"}
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request,  // Gets name, email, password from JSON
            HttpSession session) {
        try {
            LoginResponse response = authService.register(request, session);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setMessage("Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Exception handler for validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<LoginResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String message = error.getDefaultMessage();
            if (errorMessage.length() > 0) {
                errorMessage.append(", ");
            }
            errorMessage.append(message);
        });
        
        LoginResponse errorResponse = new LoginResponse();
        errorResponse.setMessage(errorMessage.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
