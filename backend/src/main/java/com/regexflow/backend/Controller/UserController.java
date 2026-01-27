package com.regexflow.backend.Controller;

import com.regexflow.backend.Dto.UpdateUserRoleRequest;
import com.regexflow.backend.Dto.UserResponseDto;
import com.regexflow.backend.Service.UserService;
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
     * When someone visits: http://localhost:8080/user
     * This method will run and return all users
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
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
    @PostMapping("/create")
    public ResponseEntity<UserResponseDto> createUser(@RequestBody com.regexflow.backend.Dto.UserRequestDto request) {
        try {
            UserResponseDto newUser = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * ENDPOINT: PUT /user/{id}
     * @PutMapping("/{id}") = This handles PUT requests to /user/{id}
     * @PathVariable Long id = Gets the {id} from the URL (e.g., /user/5 → id=5)
     * @RequestBody = Gets JSON data from the request body
     * 
     * Example: PUT http://localhost:8080/user/5
     * Body: {"role": "ADMIN"}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUserRole(
            @PathVariable Long id,  // Gets {id} from URL
            @RequestBody UpdateUserRoleRequest request) {  // Gets JSON from body
        try {
            UserResponseDto updatedUser = userService.updateUserRole(id, request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
