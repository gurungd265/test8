package com.example.backend.service;

import com.example.backend.dto.ProductDto;
import com.example.backend.entity.Category;
import com.example.backend.entity.Product;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.*;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // 상품 등록
    public Product createProduct(ProductDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("該当Categoryが探せません"));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategory(category); //Category 엔티티 자체를 SET

        return productRepository.save(product);
    }

    // 전체 상품 조회
    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findAllByDeletedAtIsNull(pageable);
    }

    // 재고가 0보다 큰 상품만 조회
    public Page<Product> getProductsInStock(Pageable pageable) {
        return productRepository.findByStockQuantityGreaterThanAndDeletedAtIsNull(0, pageable);
    }

    // 개별 상품 조회 (상세페이지용)
    public Product getProductById(Long id) {
        return productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));
    }

    // 상품 검색 (부분 일치, 대소문자 무시) + 페이징처리
    public Page<Product> searchProductsByName(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndDeletedAtIsNull(keyword, pageable);
    }

    // 특정 카테고리 내 모든 상품 조회
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndDeletedAtIsNull(categoryId, pageable);
    }

    // 특정 카테고리 내 재고가 0보다 큰 상품만 조회
    public Page<Product> getProductsByCategoryInStock(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndStockQuantityGreaterThanAndDeletedAtIsNull(categoryId, 0, pageable);
    }

    // 슬러그 조회 + 페이징처리
    public Page<Product> getProductsByCategorySlug(String slug, Pageable pageable) {
        Category category = categoryRepository.findBySlug(slug);
        if (category == null) {
            return Page.empty(pageable);
        }
        return productRepository.findByCategoryIdAndDeletedAtIsNull(category.getId(), pageable);
    }

    // 상품 수정
    public Product updateProduct(Long id, ProductDto dto) {
        Product existing = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setStockQuantity(dto.getStockQuantity());

        // category 변경도 허용할 경우
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedAtIsNull(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("カテゴリーが見つかりません"));
            existing.setCategory(category);
        }

        return productRepository.save(existing);
    }

    // 상품 소프트 삭제
    public void softDeleteProduct(Long id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));
        product.setDeletedAt(LocalDateTime.now(ZoneId.of("Asia/Tokyo")));
        productRepository.save(product);
    }

}
