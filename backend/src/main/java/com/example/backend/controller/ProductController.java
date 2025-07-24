package com.example.backend.controller;

import com.example.backend.dto.ProductDto;
import com.example.backend.entity.Product;
import com.example.backend.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ================================================== ADMIN ONLY ==================================================
    // 상품 등록
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto dto) {
        Product created = productService.createProduct(dto);
        return ResponseEntity.ok(productService.toDto(created));
    }

    // 상품 수정
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        Product updated = productService.updateProduct(id, productDto);
        ProductDto updatedDto = productService.toDto(updated);
        return ResponseEntity.ok(updatedDto);
    }

    // 상품 소프트 삭제
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable Long id) {
        productService.softDeleteProduct(id);
        return ResponseEntity.ok().build();
    }

    // ================================================= ADMIN & USER ==================================================
    // 상품 개별 조회 (상세페이지용)
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto productDto = productService.getProductById(id);
        if (productDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productDto);
    }
    
    // 상품 전체 조회(슬러그별, 카테고리별) + 페이징 처리 + 재고에 따른 노출
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getPagedProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(defaultValue = "1") int page, // UI는 1부터
            @RequestParam(defaultValue = "10000") int size, // 크게 지정하면 페이지 구분없이 사용 가능
            @RequestParam(required = false) Boolean inStock // 재고 0 상품 노출 여부
    ) {
        int zeroBased = Math.max(page - 1, 0); // 내부적으로는 0처리
        Pageable pageable = PageRequest.of(zeroBased, size);

        if (categorySlug != null) {
            // 슬러그 지정됨 → 해당 카테고리 상품만 페이징
            return ResponseEntity.ok(productService.getProductsByCategorySlug(categorySlug, pageable));
        } else if (categoryId != null) {
            // 카테고리 지정됨 → 해당 카테고리 상품만 페이징
            return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
        } else {
            if (Boolean.TRUE.equals(inStock)) { // 재고가 0보다 큰 상품만 페이징 조회
                return ResponseEntity.ok(productService.getProductsInStock(pageable));
            } else { // 재고 상관없이 전체 상품 페이징 조회
                return ResponseEntity.ok(productService.getProducts(pageable));
            }
        }
    }

    //단일 상품 검색 (부분 일치+대소문자 무시)
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProductsByName(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100000") int size) {
        int zeroBased = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(zeroBased, size);

        Page<ProductDto> products = productService.searchProductsByName(keyword, pageable);

        if (products.isEmpty()) {
            return ResponseEntity.ok(Page.empty(pageable));
        }
        return ResponseEntity.ok(products);
    }

}
