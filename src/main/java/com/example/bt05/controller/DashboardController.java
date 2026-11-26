package com.example.bt05.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bt05.repository.UserRepository;
import com.example.bt05.service.CategoryService;

@Controller
public class DashboardController {

	private final CategoryService categories;
	private final UserRepository users;

	public DashboardController(CategoryService categories, UserRepository users) {
		this.categories = categories;
		this.users = users;
	}

	@GetMapping("/admin/dashboard")
	public String adminDashboard(Model model) {
		model.addAttribute("pageTitle", "Admin Dashboard");
		model.addAttribute("categoryCount", categories.count());
		model.addAttribute("userCount", users.count());
		return "admin/dashboard";
	}

	@GetMapping("/user/dashboard")
	public String userDashboard(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "12") int size, Model model) {
		model.addAttribute("pageTitle", "Trang chủ");
		model.addAttribute("categories", categories.visibleCategories(page, size));
		return "user/dashboard";
	}
}
