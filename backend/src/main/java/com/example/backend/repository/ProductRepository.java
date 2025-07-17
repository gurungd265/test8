package com.example.backend.repository;

import com.example.backend.entity.Category;
import com.example.backend.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    //단일 상품 검색 기능 (부분 일치, 대소문자 무시) + 페이징 처리
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    //카테고리별 조회 기능 + 페이징 처리
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
}
