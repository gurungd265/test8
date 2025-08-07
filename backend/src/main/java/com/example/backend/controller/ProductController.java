package com.example.backend.controller;

import com.example.backend.dto.ProductDto;
import com.example.backend.entity.Product;
import com.example.backend.service.ProductService;

import jakarta.validation.Valid;
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

    // 상품 등록
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto dto) {
        Product created = productService.createProduct(dto);
        ProductDto resultDto = ProductDto.fromEntity(created);
        return ResponseEntity.ok(resultDto);
    }

    // 상품 개별 조회 (상세페이지용)
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        ProductDto dto = ProductDto.fromEntity(product);
        return ResponseEntity.ok(dto);
    }
    
    // 상품 전체 조회(슬러그별, 카테고리별) + 페이징 처리 + 재고에 따른 노출
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getPagedProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(required = false) Boolean inStock
    ) {
        int zeroBased = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(zeroBased, size);

        Page<ProductDto> result;
        if (categorySlug != null) {
            result = productService.getProductsByCategorySlug(categorySlug, pageable);
        } else if (categoryId != null) {
            result = productService.getProductsByCategory(categoryId, pageable);
        } else {
            result = Boolean.TRUE.equals(inStock)
                    ? productService.getProductsInStock(pageable)
                    : productService.getProducts(pageable);
        }

        return ResponseEntity.ok(result);
    }

    //단일 상품 검색 (부분 일치+대소문자 무시)
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProductsByName(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100000") int size
    ) {
        int zeroBased = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(zeroBased, size);

        // keyword가 비어있지 않다면 부분 일치 검색
        if (keyword != null && !keyword.trim().isEmpty()) {
            Page<ProductDto> products = productService.searchProductsByName(keyword, pageable);
            return ResponseEntity.ok(products);
        }

        // keyword가 없을 경우 빈 페이지 반환
        return ResponseEntity.ok(Page.empty(pageable));
    }

    // 상품 수정
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto productDto) {
        productService.updateProduct(id, productDto);
        return ResponseEntity.ok().build();
    }

    // 상품 소프트 삭제
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable Long id) {
        productService.softDeleteProduct(id);
        return ResponseEntity.ok().build();
    }

}
