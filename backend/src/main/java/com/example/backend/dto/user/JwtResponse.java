package com.example.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
//@Setter
@AllArgsConstructor
public class JwtResponse {
    private final String token;
    private final String type;
//  private String type = "Bearer";
    private final String userEmail;
}

/*
컨트롤러에서 "Bearer" 직접 리턴

return ResponseEntity.ok(new JwtResponse(token, "Bearer", loginDto.getEmail()));

→ 이유: type = "Bearer"는 @AllArgsConstructor에 의해 덮여서 작동 안 할 수 있음
 */
