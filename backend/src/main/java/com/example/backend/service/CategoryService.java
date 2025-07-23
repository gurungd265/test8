package com.example.backend.service;

import com.example.backend.entity.Category;
import com.example.backend.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    /*
        (deleted_at IS NULL 자동 필터링)
     */

    private final CategoryRepository categoryRepository;

    // 카테고리 전체 조회
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 카테고리 단일 조회
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // 카테고리 등록
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // 카테고리 소프트 삭제 (일시만 기록, 실제 DB에는 그대로 보존)
    public void softDeleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("カテゴリーが見つかりません。"));
        category.setDeletedAt(LocalDateTime.now(ZoneId.of("Asia/Tokyo")));
        categoryRepository.save(category);
    }
}
