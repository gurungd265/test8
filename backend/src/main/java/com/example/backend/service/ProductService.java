package com.example.backend.service;

import com.example.backend.dto.ProductCharacteristicDto;
import com.example.backend.dto.ProductDto;
import com.example.backend.entity.Category;
import com.example.backend.entity.Product;
import com.example.backend.entity.ProductImage;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.ProductImageRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.*;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;

    // DTO -> Entity Packing
    public ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setStockQuantity(product.getStockQuantity());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
            dto.setCategorySlug(product.getCategory().getSlug());
        }
        List<ProductCharacteristicDto> charDtos = product.getCharacteristics() == null ?
                List.of() :
                product.getCharacteristics().stream()
                        .map(c -> new ProductCharacteristicDto(c.getCharacteristicName(), c.getCharacteristicValue()))
                        .toList();
        dto.setCharacteristics(charDtos);
        return dto;
    }

    // 상품 등록
    public Product createProduct(ProductDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("該当Categoryが探せません"));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategory(category); //Category 엔티티 자체를 SET

        return productRepository.save(product);
    }

    // 전체 상품 조회
    public Page<ProductDto> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAllWithImages(pageable);
        return products.map(this::toDto);
    }

    // 재고가 0보다 큰 상품만 조회
    public Page<ProductDto> getProductsInStock(Pageable pageable) {
        return productRepository.findByStockQuantityGreaterThanWithImages(0, pageable)
                .map(this::toDto);
    }

    // 개별 상품 조회 (상세페이지용)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));
        return toDto(product);
    }

    // 상품 검색 (부분 일치, 대소문자 무시) + 페이징처리
    public Page<ProductDto> searchProductsByName(String keyword, Pageable pageable) {
        Page<Product> products = productRepository.findByNameContainingIgnoreCaseWithImages(keyword, pageable);
        return products.map(this::toDto);
    }

    // 상품 수정
    public Product updateProduct(Long id, ProductDto dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setDiscountPrice(dto.getDiscountPrice());
        existing.setStockQuantity(dto.getStockQuantity());
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("カテゴリーが見つかりません"));
            existing.setCategory(category);
        }
        return productRepository.save(existing);
    }

    // 상품 소프트 삭제
    public void softDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));
        product.setDeletedAt(LocalDateTime.now(ZoneId.of("Asia/Tokyo")));
        productRepository.save(product);
    }

    // ===================================== Cart + ProductImage =====================================
    // 특정 카테고리 내 모든 상품 조회
    public Page<ProductDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdWithImages(categoryId, pageable)
                .map(this::toDto);
    }

    // 특정 카테고리 내 재고가 0보다 큰 상품만 조회
    public Page<ProductDto> getProductsByCategoryInStock(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndStockQuantityGreaterThanWithImages(categoryId, 0, pageable)
                .map(this::toDto);
    }

    // 슬러그 조회 + 페이징처리
    public Page<ProductDto> getProductsByCategorySlug(String slug, Pageable pageable) {
        Category category = categoryRepository.findBySlug(slug);
        if (category == null) {
            return Page.empty(pageable);
        }
        return productRepository.findByCategoryIdWithImages(category.getId(), pageable)
                .map(this::toDto);
    }

}
