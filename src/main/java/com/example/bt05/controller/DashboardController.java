package com.example.bt05.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.bt05.repository.CategoryRepository;
import com.example.bt05.repository.UserRepository;

@Controller
public class DashboardController {

	private final CategoryRepository categories;
	private final UserRepository users;

	public DashboardController(CategoryRepository categories, UserRepository users) {
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
	public String userDashboard(Model model) {
		model.addAttribute("pageTitle", "Trang người dùng");
		return "user/dashboard";
	}
}
