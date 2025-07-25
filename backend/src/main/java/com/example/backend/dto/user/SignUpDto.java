package com.example.backend.dto.user;

import com.example.backend.entity.user.Gender;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpDto {

    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    private Gender gender;

    private String phoneNumber;

}
