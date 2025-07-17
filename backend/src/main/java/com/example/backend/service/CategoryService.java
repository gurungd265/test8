package com.example.backend.service;

import com.example.backend.entity.Category;
import com.example.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 전체 카테고리 조회
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 단일 카테고리 조회
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // 카테고리 등록
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // 카테고리 삭제
    public boolean deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            return false;
        }
        categoryRepository.deleteById(id);
        return true;
    }
}
