package com.example.backend.dto.user;

import com.example.backend.entity.user.Gender;
import com.example.backend.entity.user.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAdminDto { //관리자용

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    private String phoneNumber;
    private Gender gender;

    @NotNull
    private UserType userType; //관리자가 수정 가능
}
