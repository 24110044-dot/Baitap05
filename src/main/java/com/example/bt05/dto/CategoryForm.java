package com.example.bt05.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryForm {

	@NotBlank(message = "Tên danh mục không được để trống.")
	@Size(max = 50, message = "Tên danh mục không được vượt quá 50 ký tự.")
	private String categoryName;

	@Size(max = 500, message = "Đường dẫn hình ảnh không được vượt quá 500 ký tự.")
	private String images;

	private boolean status = true;

	public String getCategoryName() { return categoryName; }
	public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
	public String getImages() { return images; }
	public void setImages(String images) { this.images = images; }
	public boolean isStatus() { return status; }
	public void setStatus(boolean status) { this.status = status; }
}
