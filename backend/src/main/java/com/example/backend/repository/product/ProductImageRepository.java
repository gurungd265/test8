package com.example.backend.repository.product;

import com.example.backend.entity.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */

    List<ProductImage> findByProductId(Long productId);

    // 대표 이미지 가져오기
    @Query("SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product.id = :productId AND pi.displayOrder = 1 AND pi.deletedAt IS NULL")
    String findMainImageUrlByProductId(@Param("productId") Long productId);
}
