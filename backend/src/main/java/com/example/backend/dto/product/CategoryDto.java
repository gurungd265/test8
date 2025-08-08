package com.example.backend.dto.product;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;

    @NotBlank(message = "カテゴリー名を入力してください。")
    private String name;

    @NotBlank(message = "スラッグを入力してください。")
    private String slug;
}
