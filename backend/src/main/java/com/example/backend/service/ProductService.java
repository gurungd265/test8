package com.example.backend.service;

import com.example.backend.dto.ProductDto;
import com.example.backend.entity.Category;
import com.example.backend.entity.Product;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductRepository;

import lombok.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

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
        product.setCreatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    // 전체 상품 조회
    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    // 개별 상품 조회 (상세페이지용)
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // 단일 상품 검색 (부분 일치, 대소문자 무시) + 페이징처리
    public Page<Product> searchProductsByName(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    // 카테고리별 조회 + 페이징처리
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    // 슬러그 조회 + 페이징처리
    public Page<Product> getProductsByCategorySlug(String slug, Pageable pageable) {
        Category category = categoryRepository.findBySlug(slug);
        if (category == null) {
            return Page.empty(pageable);
        }
        return productRepository.findByCategoryId(category.getId(), pageable);
    }

}
