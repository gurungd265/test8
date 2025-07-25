package com.example.backend.controller;

import com.example.backend.dto.ProductReviewDto;
import com.example.backend.dto.user.SignUpDto;
import com.example.backend.dto.user.UserAdminDto;
import com.example.backend.entity.user.User;
import com.example.backend.service.ProductReviewService;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.backend.dto.user.UserResponseDto;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProductReviewService reviewService;

    // 회원가입 (사용자 등록)
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpDto dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("登録が完了しました。");
    }

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body("認証されていないユーザーです。");
        }

        UserResponseDto userDto=UserResponseDto.fromEntity(user);
        return ResponseEntity.ok(userDto);
    }

    // 현재 로그인한 사용자 정보 수정
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserResponseDto dto) {
        if (user == null) {
            return ResponseEntity.status(401).body("認証されていないユーザーです。");
        }
        userService.updateUser(user.getId(), dto);
        return ResponseEntity.noContent().build();
    }

    // ========================================== ProductReview ==========================================
    // 현재 로그인한 사용자가 작성한 상품 리뷰 목록 조회
    @GetMapping("/me/reviews")
    public ResponseEntity<Page<ProductReviewDto>> getUserReviews(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        Page<ProductReviewDto> reviews = reviewService.getReviewsByUser(user.getId(), pageable);
        return ResponseEntity.ok(reviews);
    }

    // =========================================== AdminOnly =============================================
    // 관리자용 유저 정보 수정
    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUserByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody UserAdminDto dto) {
        userService.updateUserByAdmin(id, dto);
    }

    // 관리자용 유저 삭제
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserByAdmin(@PathVariable Long id) {
        userService.deleteUserByAdmin(id);
    }

}