package com.example.backend.repository;

import com.example.backend.entity.Category;
import com.example.backend.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
    // 상품 전체 조회 + 페이징 + 이미지
    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.productImages",
            countQuery = "SELECT COUNT(p) FROM Product p")
    Page<Product> findAllWithImages(Pageable pageable);

    // 재고 있는 상품만 전체 조회 + 페이징 + 이미지
    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.productImages WHERE p.stockQuantity > :stock",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE p.stockQuantity > :stock")
    Page<Product> findByStockQuantityGreaterThanWithImages(@Param("stock") int stock, Pageable pageable);

    // 상품 상세 페이지용 개별 조회

    // 1) 이미지만 fetch join
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productImages WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    // 2) 속성만 fetch join
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.options WHERE p.id = :id")
    Optional<Product> findByIdWithoptions(@Param("id") Long id);

    // 상품 검색(부분 일치, 대소문자 무시) + 페이징 + 이미지
    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.productImages WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> findByNameContainingIgnoreCaseWithImages(@Param("keyword") String keyword, Pageable pageable);

    // 카테고리별 조회 + 페이징 + 이미지
    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.productImages WHERE p.category.id = :categoryId",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryIdWithImages(@Param("categoryId") Long categoryId, Pageable pageable);

    // 특정 카테고리 내 재고 있는 상품만 전체 조회 + 페이징 + 이미지
    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.productImages WHERE p.category.id = :categoryId AND p.stockQuantity > :stock",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.stockQuantity > :stock")
    Page<Product> findByCategoryIdAndStockQuantityGreaterThanWithImages(@Param("categoryId") Long categoryId, @Param("stock") int stock, Pageable pageable);
}
