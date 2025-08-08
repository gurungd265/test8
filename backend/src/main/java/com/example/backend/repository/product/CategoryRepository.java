package com.example.backend.repository.product;

import com.example.backend.entity.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug); //슬러그명 중복체크
    boolean existsByName(String name); //카테고리명 중복체크
}
