package com.example.backend.service;

import com.example.backend.dto.CategoryDto;
import com.example.backend.entity.Category;
import com.example.backend.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 카테고리 전체 조회 (DTO 리스트 반환)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 카테고리 단일 조회 (DTO 반환)
    public Optional<CategoryDto> getCategoryById(Long id) {
        return categoryRepository.findById(id).map(this::toDto);
    }

    // 카테고리 등록 (DTO 입력, Entity 저장 후 DTO 반환)
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = toEntity(dto);
        Category saved = categoryRepository.save(category);
        return toDto(saved);
    }

    // 카테고리 소프트 삭제 (id로 조회 후 deletedAt 설정)
    public void softDeleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("カテゴリーが見つかりません。"));
        category.setDeletedAt(LocalDateTime.now(ZoneId.of("Asia/Tokyo")));
        categoryRepository.save(category);
    }

    // 편의 메소드 ==========================================================================================
    // Entity -> DTO 변환
    private CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        return dto;
    }

    // DTO -> Entity 변환
    private Category toEntity(CategoryDto dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        return category;
    }

}
