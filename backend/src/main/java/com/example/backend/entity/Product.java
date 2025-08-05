package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL") //기본 조회 시 자동으로 삭제된 데이터는 제외
public class Product {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "discount_price", nullable = true)
    private BigDecimal discountPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // =================================== Category ===================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id") //PK(category_id)를 JPA가 자동 관리
    private Category category;

    // ============================= ProductCharacteristic =============================
    @OneToMany(mappedBy = "product")
    private List<ProductCharacteristic> characteristics;

    // ================================= ProductReview =================================
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductReview> reviews;

    // ================================= ProductImage =================================
    // ProductImageRepository 쿼리 참고
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>();

    //대표 이미지가져오기
    public String getMainImageUrl() {
        return productImages.stream()
                .filter(img -> img.getDeletedAt() == null)   // soft delete 체크
                .sorted(Comparator.comparingInt(ProductImage::getDisplayOrder))
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    public void addProductImage(ProductImage image) {
        productImages.add(image);
        image.setProduct(this);
    }

    public void removeProductImage(ProductImage image) {
        productImages.remove(image);
        image.setProduct(null);
    }

}
