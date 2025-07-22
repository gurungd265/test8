package com.example.backend.repository;

import com.example.backend.entity.Category;
import com.example.backend.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    //상품 전체 조회 + 페이징 처리
    //소프트 삭제 제외
    Page<Product> findAllByDeletedAtIsNull(Pageable pageable);

    // 재고 있는 상품 전체 조회 + 페이징 처리
    // 소프트 삭제 제외
    Page<Product> findByStockQuantityGreaterThanAndDeletedAtIsNull(int i, Pageable pageable);

    //개별 상품 상세페이지용
    //소프트 삭제 제외
    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    //상품 검색(부분 일치, 대소문자 무시) + 페이징 처리
    //소프트 삭제 제외
    Page<Product> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String keyword, Pageable pageable);

    //카테고리별 조회 + 페이징 처리
    //소프트 삭제 제외
    Page<Product> findByCategoryIdAndDeletedAtIsNull(Long categoryId, Pageable pageable);

    // 특정 카테고리 내 재고 0 초과 상품 + 페이징 처리
    // 소프트 삭제 제외
    Page<Product> findByCategoryIdAndStockQuantityGreaterThanAndDeletedAtIsNull(Long categoryId, int minStockQuantity, Pageable pageable);
}
