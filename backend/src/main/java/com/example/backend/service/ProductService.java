package com.example.backend.service;

import com.example.backend.dto.ProductDto;
import com.example.backend.entity.Category;
import com.example.backend.entity.Product;
import com.example.backend.entity.Wishlist;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.service.CartService;

import com.example.backend.repository.WishlistRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.*;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final CartService cartService;

    // DTO -> Entity Packing
    public ProductDto toDto(Product product) {
        return ProductDto.fromEntity(product);
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
    @Transactional
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
    @Transactional
    public void softDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));
        product.setDeletedAt(LocalDateTime.now(ZoneId.of("Asia/Tokyo")));
        productRepository.save(product);
        // 카트에서도 소프트 삭제
        cartService.softDeleteCartItemsByProductId(id);
        // 위시리스트에서도 소프트 삭제
        List<Wishlist> wishlists = wishlistRepository.findByProductId(id);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
        for (Wishlist wishlist : wishlists) {
            wishlist.setDeletedAt(now);
        }
        wishlistRepository.saveAll(wishlists);

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
        Optional<Category> categoryOpt = categoryRepository.findBySlug(slug);
        if (categoryOpt.isEmpty()) {
            return Page.empty(pageable);
        }
        Category category = categoryOpt.get();
        return productRepository.findByCategoryIdWithImages(category.getId(), pageable)
                .map(this::toDto);
    }

}
