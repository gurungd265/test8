package com.example.backend.dto.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReviewDto {

    private Long id;
    private Long productId;
    private Long userId;

    @NotNull(message = "1から5までの数で評価してください。")
    @Min(1)
    @Max(5)
    private Byte rating;

    @NotBlank(message = "内容を入力してください。")
    @Size(min = 10, message = "レビュー内容は最低10文字以上で入力してください。")
    private String reviewText;

    private Boolean isApproved;
    private LocalDateTime createdAt;

}
