package com.example.backend.controller;

import com.example.backend.dto.JwtResponse;
import com.example.backend.dto.LoginDto;
import com.example.backend.jwt.JwtToken;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// JWT 기반 로그인 기능을 처리하는 핵심 컨트롤러
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor //final 필드를 자동 주입 (constructor 생성됨)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtToken jwtToken;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtToken.generateToken(authentication);

            return ResponseEntity.ok(new JwtResponse(token, "Bearer", authentication.getName()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("ログイン失敗: e-mailまたはパスワードを確認してください。");
        }
    }



}
