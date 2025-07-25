package com.example.backend.dto.user;

import com.example.backend.entity.user.Gender;
import com.example.backend.entity.user.User;
import com.example.backend.entity.user.UserType;
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
    private Gender gender;
    private UserType userType;
    private LocalDateTime createdAt;

    public static UserResponseDto fromEntity(User user){
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .userType(user.getUserType())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
