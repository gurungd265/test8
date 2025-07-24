package com.example.backend.dto;

import com.example.backend.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDto {
    private long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String userType;
    private LocalDateTime createdAt;

    public static UserResponseDto fromEntity(User user){
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .userType(user.getUserType().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
