package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.LoginRequest;
import com.regexflow.backend.Dto.LoginResponse;
import com.regexflow.backend.Dto.RegisterRequest;
import com.regexflow.backend.Dto.UserResponseDto;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Enums.UserRole;
import com.regexflow.backend.Mapper.UsersMapper;
import com.regexflow.backend.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request, HttpSession session) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate token (using UUID for simplicity)
        String token = UUID.randomUUID().toString();
        
        // Store token in session
        session.setAttribute("token", token);
        session.setAttribute("userId", user.getUId());
        session.setAttribute("userRole", user.getRole().name());

        LoginResponse response = new LoginResponse();
        response.setMessage("Login successful");
        response.setToken(token);
        response.setSessionId(session.getId());
        response.setUser(UsersMapper.toDto(user));

        return response;
    }

    public LoginResponse register(RegisterRequest request, HttpSession session) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Users newUser = new Users();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(UserRole.CUSTOMER); // Default role

        Users savedUser = userRepository.save(newUser);

        // Auto-login after registration
        String token = UUID.randomUUID().toString();
        session.setAttribute("token", token);
        session.setAttribute("userId", savedUser.getUId());
        session.setAttribute("userRole", savedUser.getRole().name());

        LoginResponse response = new LoginResponse();
        response.setMessage("Registration successful");
        response.setToken(token);
        response.setSessionId(session.getId());
        response.setUser(UsersMapper.toDto(savedUser));

        return response;
    }
}
