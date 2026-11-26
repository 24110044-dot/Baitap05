package com.example.bt05.controller;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bt05.dto.CategoryForm;
import com.example.bt05.entity.Category;
import com.example.bt05.service.CategoryImageStorageService;
import com.example.bt05.service.CategoryService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

	private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

	private final CategoryService categories;
	private final CategoryImageStorageService images;

	public CategoryController(CategoryService categories, CategoryImageStorageService images) {
		this.categories = categories;
		this.images = images;
	}

	@GetMapping
	public String list(@RequestParam(defaultValue = "") String keyword,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, Model model) {
		Page<Category> result = categories.search(keyword, page, size);
		model.addAttribute("categories", result);
		model.addAttribute("keyword", keyword);
		model.addAttribute("size", result.getSize());
		model.addAttribute("pageTitle", "Danh mục");
		return "admin/categories/list";
	}

	@GetMapping("/create")
	public String create(Model model) {
		model.addAttribute("form", new CategoryForm());
		model.addAttribute("formAction", "/admin/categories/store");
		model.addAttribute("pageTitle", "Thêm danh mục");
		return "admin/categories/form";
	}

	@PostMapping("/store")
	public String store(@Valid @ModelAttribute("form") CategoryForm form, BindingResult result,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			Model model, RedirectAttributes redirect) {
		if (result.hasErrors() || !prepareImage(form, imageFile, result, model)) {
			return categoryForm(model, "/admin/categories/store", "Thêm danh mục");
		}
		try {
			categories.save(form, null);
			redirect.addFlashAttribute("success", "Đã thêm danh mục.");
			return "redirect:/admin/categories";
		} catch (IllegalArgumentException exception) {
			model.addAttribute("error", exception.getMessage());
			return categoryForm(model, "/admin/categories/store", "Thêm danh mục");
		}
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Integer id, Model model) {
		Category category = categories.get(id);
		CategoryForm form = new CategoryForm();
		form.setCategoryName(category.getCategoryName());
		form.setImages(category.getImages());
		form.setStatus(category.isStatus());
		model.addAttribute("form", form);
		model.addAttribute("formAction", "/admin/categories/update/" + id);
		model.addAttribute("pageTitle", "Chỉnh sửa danh mục");
		return "admin/categories/form";
	}

	@PostMapping("/update/{id}")
	public String update(@PathVariable Integer id, @Valid @ModelAttribute("form") CategoryForm form,
			BindingResult result, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			Model model, RedirectAttributes redirect) {
		if (result.hasErrors()
				|| !prepareImage(form, imageFile, result, model)) {
			return categoryForm(model, "/admin/categories/update/" + id, "Chỉnh sửa danh mục");
		}
		try {
			categories.save(form, id);
			redirect.addFlashAttribute("success", "Đã cập nhật danh mục.");
			return "redirect:/admin/categories";
		} catch (IllegalArgumentException exception) {
			model.addAttribute("error", exception.getMessage());
			return categoryForm(model, "/admin/categories/update/" + id, "Chỉnh sửa danh mục");
		}
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
		try {
			categories.delete(id);
			redirect.addFlashAttribute("success", "Đã xóa danh mục.");
		} catch (IllegalArgumentException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/categories";
	}

	private boolean prepareImage(CategoryForm form, MultipartFile imageFile, BindingResult result, Model model) {
		String imageUrl = form.getImages();
		boolean hasUpload = imageFile != null && !imageFile.isEmpty();
		if (hasUpload && imageUrl != null && !imageUrl.isBlank()) {
			result.rejectValue("images", "category.image.source",
					"Chỉ nhập đường dẫn ảnh hoặc chọn tệp từ máy, không dùng cả hai.");
			return false;
		}

		if (hasUpload) {
			try {
				form.setImages(images.store(imageFile));
			} catch (IllegalArgumentException exception) {
				result.rejectValue("images", "category.image.invalid", exception.getMessage());
				return false;
			} catch (IOException exception) {
				logger.error("Could not save uploaded category image", exception);
				model.addAttribute("error", "Không thể lưu hình ảnh đã chọn. Vui lòng thử lại.");
				return false;
			}
		} else if (imageUrl != null && !imageUrl.isBlank()) {
			String normalizedUrl = imageUrl.trim();
			if (!isAllowedImageLocation(normalizedUrl)) {
				result.rejectValue("images", "category.image.url",
						"Đường dẫn ảnh phải là URL HTTP/HTTPS hợp lệ hoặc ảnh đã tải lên.");
				return false;
			}
			form.setImages(normalizedUrl);
		}
		return true;
	}

	private boolean isAllowedImageLocation(String value) {
		String uploadPrefix = "/uploads/categories/";
		if (value.startsWith(uploadPrefix)) {
			String fileName = value.substring(uploadPrefix.length());
			int extensionIndex = fileName.lastIndexOf('.');
			if (extensionIndex < 0) {
				return false;
			}
			String extension = fileName.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
			try {
				String uuid = UUID.fromString(fileName.substring(0, extensionIndex)).toString();
				return fileName.equals(uuid + "." + extension)
						&& (extension.equals("png") || extension.equals("jpg") || extension.equals("gif"));
			} catch (IllegalArgumentException exception) {
				return false;
			}
		}

		try {
			URI uri = URI.create(value);
			return uri.getHost() != null
					&& ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()));
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}

	private String categoryForm(Model model, String formAction, String pageTitle) {
		model.addAttribute("formAction", formAction);
		model.addAttribute("pageTitle", pageTitle);
		return "admin/categories/form";
	}
}
