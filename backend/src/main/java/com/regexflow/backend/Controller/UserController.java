package com.regexflow.backend.Controller;

import com.regexflow.backend.Dto.UpdateUserRoleRequest;
import com.regexflow.backend.Dto.UserResponseDto;
import com.regexflow.backend.Enums.UserRole;
import com.regexflow.backend.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This is a REST Controller - it handles HTTP requests
 * @RequestMapping("/user") means all endpoints here start with /user
 */
@RestController
@RequestMapping("/user")
public class UserController {

    // @Autowired tells Spring to automatically create and inject UserService
    @Autowired
    private UserService userService;

    /**
     * ENDPOINT: GET /user
     * @GetMapping = This handles GET requests
     * Only ADMIN users can access this endpoint to view all users
     * 
     * Example: GET http://localhost:8080/user
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(HttpSession session) {
        // Get current user's role from session
        String currentUserRole = (String) session.getAttribute("userRole");
        
        // Only ADMIN can view all users
        if (currentUserRole == null || !currentUserRole.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * ENDPOINT: POST /user/create
     * @PostMapping("/create") = This handles POST requests to /user/create
     * This endpoint allows ADMIN to create users with specific roles (MAKER, CHECKER, ADMIN)
     * Regular users should use /auth/register which defaults to CUSTOMER
     * 
     * Example: POST http://localhost:8080/user/create
     * Body: {"name": "Jane Maker", "email": "maker@example.com", "password": "pass123", "role": "MAKER"}
     */
    
    // @PostMapping("/create")
    // public ResponseEntity<UserResponseDto> createUser(@RequestBody com.regexflow.backend.Dto.UserRequestDto request) {
    //     try {
    //         UserResponseDto newUser = userService.createUser(request);
    //         return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    //     }
    // }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUserRole(
            @PathVariable Long id,  // Gets {id} from URL
            @RequestBody UpdateUserRoleRequest request,  // Gets JSON from body
            HttpSession session) {  // Gets current user session
        try {
            // Prevent updating to ADMIN role - only one admin should exist
            if (request.getRole() == UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .build(); // Service will throw exception with proper message
            }
            
            // Check if trying to update to MAKER or CHECKER
            if (request.getRole() == UserRole.MAKER || request.getRole() == UserRole.CHECKER) {
                // Get current user's role from session
                String currentUserRole = (String) session.getAttribute("userRole");
                
                // Only ADMIN can update roles to MAKER or CHECKER
                if (currentUserRole == null || !currentUserRole.equals("ADMIN")) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }           
            UserResponseDto updatedUser = userService.updateUserRole(id, request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            // Handle "Cannot update to ADMIN" or "User not found" errors
            if (e.getMessage() != null && e.getMessage().contains("ADMIN")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}