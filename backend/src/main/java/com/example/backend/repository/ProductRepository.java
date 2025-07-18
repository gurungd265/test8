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
    //소프트삭제되지 않은 것만
    Page<Product> findAllByDeletedAtIsNull(Pageable pageable);

    //개별 상품 상세페이지용
    //소프트삭제되지 않은 것만
    Optional<Product> findByIdAndDeletedAtIsNull(Long id);
    
    //상품 검색(부분 일치, 대소문자 무시) + 페이징 처리
    //소프트 삭제되지 않은 것만
    Page<Product> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String keyword, Pageable pageable);

    //카테고리별 조회 + 페이징 처리
    //소프트 삭제되지 않은 것만
    Page<Product> findByCategoryIdAndDeletedAtIsNull(Long categoryId, Pageable pageable);
}
