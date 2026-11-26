package com.example.bt05.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bt05.dto.CategoryForm;
import com.example.bt05.entity.Category;
import com.example.bt05.repository.CategoryRepository;

@Service
public class CategoryService {

	private final CategoryRepository categories;

	public CategoryService(CategoryRepository categories) {
		this.categories = categories;
	}

	@Transactional(readOnly = true)
	public Page<Category> search(String keyword, int page, int size) {
		String query = keyword == null ? "" : keyword.trim();
		return categories.findByCategoryNameContainingIgnoreCase(query,
				PageRequest.of(Math.max(page, 0), normalizeSize(size), Sort.by("categoryId").descending()));
	}

	@Transactional(readOnly = true)
	public Page<Category> visibleCategories(int page, int size) {
		return categories.findByStatusTrue(
				PageRequest.of(Math.max(page, 0), normalizeSize(size), Sort.by("categoryId").descending()));
	}

	@Transactional(readOnly = true)
	public long count() {
		return categories.count();
	}

	@Transactional(readOnly = true)
	public Category get(Integer id) {
		return categories.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục."));
	}

	@Transactional
	public Category save(CategoryForm form, Integer id) {
		String name = form.getCategoryName().trim();
		boolean duplicate = id == null
				? categories.existsByCategoryNameIgnoreCase(name)
				: categories.existsByCategoryNameIgnoreCaseAndCategoryIdNot(name, id);
		if (duplicate) {
			throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
		}
		Category category = id == null ? new Category() : get(id);
		category.setCategoryName(name);
		category.setImages(blankToNull(form.getImages()));
		category.setStatus(form.isStatus());
		return categories.save(category);
	}

	@Transactional
	public void delete(Integer id) {
		categories.delete(get(id));
	}

	private int normalizeSize(int size) {
		return size < 5 ? 5 : Math.min(size, 50);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
