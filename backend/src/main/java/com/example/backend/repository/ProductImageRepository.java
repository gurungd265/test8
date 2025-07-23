package com.example.backend.repository;

import com.example.backend.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
    List<ProductImage> findByProductId(Long productId);
}
