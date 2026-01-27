package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.UpdateUserRoleRequest;
import com.regexflow.backend.Dto.UserRequestDto;
import com.regexflow.backend.Dto.UserResponseDto;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Enums.UserRole;
import com.regexflow.backend.Mapper.UsersMapper;
import com.regexflow.backend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UsersMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto updateUserRole(Long userId, UpdateUserRoleRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Prevent updating to ADMIN role - only one admin should exist
        if (request.getRole() == UserRole.ADMIN) {
            // Check if there's already an admin (and it's not the current user being updated)
            boolean adminExists = userRepository.findAll().stream()
                    .anyMatch(u -> u.getRole() == UserRole.ADMIN && !u.getUId().equals(userId));
            
            if (adminExists) {
                throw new RuntimeException("Cannot update to ADMIN role. Only one admin can exist in the system.");
            }
        }
        
        user.setRole(request.getRole());
        Users updatedUser = userRepository.save(user);
        
        return UsersMapper.toDto(updatedUser);
    }

    public Users getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    /**
     * Create a new user with a specific role
     * This is used by ADMIN to create MAKER or CHECKER users
     * Regular users should use /auth/register which defaults to CUSTOMER
     * Cannot create ADMIN users - only one admin exists (created by DataInitializer)
     */
    public UserResponseDto createUser(UserRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Prevent creating ADMIN users - only one admin should exist
        if (request.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Cannot create ADMIN user. Only one admin can exist in the system.");
        }

        Users newUser = UsersMapper.toEntity(request);
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        Users savedUser = userRepository.save(newUser);
        return UsersMapper.toDto(savedUser);
    }
}
