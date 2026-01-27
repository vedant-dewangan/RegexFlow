package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.UpdateUserRoleRequest;
import com.regexflow.backend.Dto.UserRequestDto;
import com.regexflow.backend.Dto.UserResponseDto;
import com.regexflow.backend.Entity.Users;
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
     * This is used by ADMIN to create MAKER, CHECKER, or other ADMIN users
     * Regular users should use /auth/register which defaults to CUSTOMER
     */
    public UserResponseDto createUser(UserRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Users newUser = UsersMapper.toEntity(request);
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        Users savedUser = userRepository.save(newUser);
        return UsersMapper.toDto(savedUser);
    }
}
