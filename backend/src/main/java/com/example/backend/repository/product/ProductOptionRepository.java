package com.example.backend.repository.product;

import com.example.backend.entity.product.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    // 기본 JpaRepository 기능만으로 충분합니다.
}