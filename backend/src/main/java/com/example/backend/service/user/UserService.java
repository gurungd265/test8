package com.example.backend.service.user;

import com.example.backend.dto.user.SignUpDto;
import com.example.backend.dto.user.UserAdminDto;
import com.example.backend.dto.user.UserResponseDto;
import com.example.backend.entity.user.User;
import com.example.backend.entity.user.UserType;
import com.example.backend.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 유저 등록
    public void registerUser(SignUpDto dto){

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("既に存在します。");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber() != null ? dto.getPhoneNumber() : "")
                .gender(dto.getGender())  // enum
                .userType(UserType.USER)  // enum
                .build();

        userRepository.save(user);
    }

    // 유저 정보 수정
    @Transactional
    public void updateUser(Long userId, UserResponseDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが存在しません。"));
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            user.setLastName(dto.getLastName());
        }
        user.setPhoneNumber(dto.getPhoneNumber());  // nullable
        user.setGender(dto.getGender());  // nullable

        userRepository.save(user);
    }

    // 관리자용 유저 정보 수정 (모든 필드 수정 가능)
    @Transactional
    public void updateUserByAdmin(Long userId, UserAdminDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが存在しません。"));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setGender(dto.getGender());
        user.setUserType(dto.getUserType());

        userRepository.save(user);
    }

    // 관리자용 유저 삭제
    @Transactional
    public void deleteUserByAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが存在しません。"));
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
