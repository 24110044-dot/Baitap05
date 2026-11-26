package com.example.bt05.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.bt05.service.CategoryImageStorageService;

@Configuration
public class CategoryImageWebConfig implements WebMvcConfigurer {

	private final CategoryImageStorageService images;

	public CategoryImageWebConfig(CategoryImageStorageService images) {
		this.images = images;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/categories/**")
				.addResourceLocations(images.resourceLocation());
	}
}
