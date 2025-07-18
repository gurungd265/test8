package com.example.backend.repository;

import com.example.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 소프트 삭제되지 않은 카테고리 전체 조회
    List<Category> findByDeletedAtIsNull();

    // 소프트 삭제되지 않은 카테고리 단일 조회
    Optional<Category> findByIdAndDeletedAtIsNull(Long id);

    Category findBySlug(String slug);
}
