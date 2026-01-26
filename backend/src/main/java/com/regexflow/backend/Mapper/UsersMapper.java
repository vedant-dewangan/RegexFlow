package com.regexflow.backend.Mapper;

import com.regexflow.backend.Dto.UserRequestDto;
import com.regexflow.backend.Dto.UserResponseDto;
import com.regexflow.backend.Entity.Users;

public class UsersMapper {

    // Hide sensitive info from others
    public static UserResponseDto toDto(Users user) {
        if (user == null) {
            return null;
        }
        UserResponseDto dto = new UserResponseDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        return dto;
    }

    public static Users toEntity(UserRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Users user = new Users();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());

        return user;
    }
}
