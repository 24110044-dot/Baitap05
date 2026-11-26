package com.example.bt05.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bt05.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

	Page<Category> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);

	Page<Category> findByStatusTrue(Pageable pageable);

	boolean existsByCategoryNameIgnoreCase(String categoryName);

	boolean existsByCategoryNameIgnoreCaseAndCategoryIdNot(String categoryName, Integer categoryId);
}
