package com.example.backend.controller.user;

import com.example.backend.dto.product.ProductReviewDto;
import com.example.backend.dto.user.SignUpDto;
import com.example.backend.dto.user.UserAdminDto;
import com.example.backend.entity.user.User;
import com.example.backend.service.product.ProductReviewService;
import com.example.backend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.backend.dto.user.UserResponseDto;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProductReviewService reviewService;
    private final PasswordEncoder passwordEncoder;

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

    // 새롭게 추가된 비밀번호 확인 엔드포인트
    @PostMapping("/me/confirm-password")
    public ResponseEntity<Map<String,Boolean>> confirmPassword(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String,String> request){
        String password = request.get("password");
        if(user == null){
            Map<String,Boolean> response= new HashMap<>();
            response.put("valid",false);
            return ResponseEntity.status(401).body(response);
        }

        if(password==null||password.isEmpty()){
            Map<String,Boolean> response= new HashMap<>();
            response.put("valid",false);
            return ResponseEntity.badRequest().body(response);
        }

        boolean isValid= passwordEncoder.matches(password,user.getPasswordHash());
        Map<String,Boolean> response = new HashMap<>();
        response.put("valid",isValid);
        return ResponseEntity.ok(response);
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