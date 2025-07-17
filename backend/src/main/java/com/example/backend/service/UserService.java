package com.example.backend.service;

import com.example.backend.dto.SignUpDto;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
                .build();

        userRepository.save(user);
    }
}
