package com.example.backend.entity.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "Product_images")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL") // 소프트 삭제 필터
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Product와 다대일 (여러 이미지가 하나의 상품에 속함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "image_url", nullable = false, columnDefinition = "Text")
    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder; //0, 1...

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
