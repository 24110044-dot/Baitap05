package com.example.bt05.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bt05.dto.RegisterForm;
import com.example.bt05.service.AccountService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

	private final AccountService accounts;

	public AuthController(AccountService accounts) {
		this.accounts = accounts;
	}

	@GetMapping("/")
	public String home(Authentication authentication) {
		if (authentication == null) {
			return "redirect:/login";
		}
		return authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))
						? "redirect:/admin/dashboard" : "redirect:/user/dashboard";
	}

	@GetMapping("/login")
	public String login(@RequestParam(required = false) String error,
			@RequestParam(required = false) String logout, Model model) {
		model.addAttribute("pageTitle", "Đăng nhập");
		if (error != null) {
			model.addAttribute("error", "Thông tin đăng nhập không hợp lệ hoặc tài khoản đã bị khóa.");
		}
		if (logout != null) {
			model.addAttribute("success", "Bạn đã đăng xuất an toàn.");
		}
		return "auth/login";
	}

	@GetMapping("/register")
	public String registerForm(Model model) {
		model.addAttribute("form", new RegisterForm());
		model.addAttribute("pageTitle", "Tạo tài khoản");
		return "auth/register";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("form") RegisterForm form,
			BindingResult result, Model model, RedirectAttributes redirect) {
		if (result.hasErrors()) {
			model.addAttribute("pageTitle", "Tạo tài khoản");
			return "auth/register";
		}
		try {
			accounts.register(form);
			redirect.addFlashAttribute("success", "Đăng ký thành công. Hãy đăng nhập bằng tài khoản mới.");
			return "redirect:/login";
		} catch (IllegalArgumentException exception) {
			model.addAttribute("error", exception.getMessage());
			model.addAttribute("pageTitle", "Tạo tài khoản");
			return "auth/register";
		}
	}
}
