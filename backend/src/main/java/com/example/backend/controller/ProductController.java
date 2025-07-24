package com.example.backend.controller;

import com.example.backend.dto.ProductDto;
import com.example.backend.dto.ProductResponseDto;
import com.example.backend.entity.Product;
import com.example.backend.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 상품 등록
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestParam ProductDto dto) {
        Product created = productService.createProduct(dto);
        return ResponseEntity.ok(created);
    }

    // 상품 개별 조회 (상세페이지용)
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }
    
    // 상품 전체 조회(슬러그별, 카테고리별) + 페이징 처리 + 재고에 따른 노출
    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getPagedProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(defaultValue = "1") int page, // UI는 1부터
            @RequestParam(defaultValue = "10000") int size, // 크게 지정하면 페이지 구분없이 사용 가능
            @RequestParam(required = false) Boolean inStock // 재고 0 상품 노출 여부
    ) {
        int zeroBased = Math.max(page - 1, 0); // 내부적으로는 0처리
        Pageable pageable = PageRequest.of(zeroBased, size);

        Page<Product> productPage;

        if (categorySlug != null) {
            // 슬러그 지정됨 → 해당 카테고리 상품만 페이징
            productPage=productService.getProductsByCategorySlug(categorySlug, pageable);
        } else if (categoryId != null) {
            // 카테고리 지정됨 → 해당 카테고리 상품만 페이징
            productPage=productService.getProductsByCategory(categoryId, pageable);
        } else {
            if (Boolean.TRUE.equals(inStock)) { // 재고가 0보다 큰 상품만 페이징 조회
                productPage=productService.getProductsInStock(pageable);
            } else { // 재고 상관없이 전체 상품 페이징 조회
                productPage=productService.getProducts(pageable);
            }
        }

        Page<ProductResponseDto> dtoPage = productPage.map(ProductResponseDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }

    //단일 상품 검색 (부분 일치+대소문자 무시)
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProductsByName(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100000") int size) {
        int zeroBased = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(zeroBased, size);

        Page<Product> products = productService.searchProductsByName(keyword, pageable);

        if (products.isEmpty()) {
            return ResponseEntity.ok(Page.empty(pageable));
        }
        return ResponseEntity.ok(products);
    }

    // 상품 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        Product updated = productService.updateProduct(id, productDto);
        return ResponseEntity.ok().build();
    }

    // 상품 소프트 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable Long id) {
        productService.softDeleteProduct(id);
        return ResponseEntity.ok().build();
    }



}
