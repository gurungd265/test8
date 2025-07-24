package com.example.backend.service;

import com.example.backend.dto.SignUpDto;
import com.example.backend.dto.UserResponseDto;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .firstName("")  // 혹은 null 또는 빈 문자열
                .lastName("")
                .phoneNumber("")
                .build(); //유저 타입값은 자동으로 USER

        userRepository.save(user);
    }

    // 유저 정보 수정
    @Transactional
    public void updateUser(Long userId, UserResponseDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません。"));

        // 이메일 변경 불가, password도 수정 안함 (필요시 별도 API)
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());

        userRepository.save(user);
    }
}
