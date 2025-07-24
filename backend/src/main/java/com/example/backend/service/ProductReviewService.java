package com.example.backend.service;

import com.example.backend.dto.ProductReviewDto;
import com.example.backend.entity.Product;
import com.example.backend.entity.ProductReview;
import com.example.backend.entity.User;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.ProductReviewRepository;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductReviewDto toDto(ProductReview review) {
        return new ProductReviewDto(
                review.getId(),
                review.getProduct().getId(),
                review.getUser().getId(),
                review.getRating(),
                review.getReviewText(),
                review.getIsApproved(),
                review.getCreatedAt()
        );
    }
    
    // 리뷰 등록
    public ProductReviewDto createReview(ProductReviewDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません。"));

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(dto.getRating());
        review.setReviewText(dto.getReviewText());
        review.setIsApproved(false); // 기본 승인 false

        ProductReview saved = reviewRepository.save(review);
        return toDto(saved);
    }
    
    // 상품별 리뷰 목록 조회 (페이징)
    public Page<ProductReviewDto> getReviewsByProduct(Long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));

        Page<ProductReview> page = reviewRepository.findByProductId(productId, pageable);
        return page.map(this::toDto);
    }

    // 유저별 리뷰 목록 조회
    public Page<ProductReviewDto> getReviewsByUser(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(this::toDto);
    }

    // 리뷰 수정
    @Transactional
    public void updateReview(ProductReviewDto dto) {
        ProductReview review = reviewRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("レビューが見つかりません。"));
        review.setRating(dto.getRating());
        review.setReviewText(dto.getReviewText());
        reviewRepository.save(review);
    }

    // 리뷰 삭제
    @Transactional
    public void softDeleteReview(ProductReviewDto dto) {
        ProductReview review = reviewRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("レビューが見つかりません。"));
        review.setDeletedAt(java.time.LocalDateTime.now());
        reviewRepository.save(review);
    }

    //isApproved... AdminOnly
}
