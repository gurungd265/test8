package com.example.backend.controller;

import com.example.backend.dto.ProductReviewDto;
import com.example.backend.dto.SignUpDto;
import com.example.backend.entity.User;
import com.example.backend.service.ProductReviewService;
import com.example.backend.service.ProductService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.backend.dto.UserResponseDto;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProductReviewService reviewService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body("認証されていないユーザーです。");
        }

        UserResponseDto userDto=UserResponseDto.fromEntity(user);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpDto dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("登録が完了しました。");
    }

    //======================================== ProductReview ========================================
    @GetMapping("/me/reviews")
    public ResponseEntity<Page<ProductReviewDto>> getUserReviews(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<ProductReviewDto> reviews = reviewService.getReviewsByUser(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

}