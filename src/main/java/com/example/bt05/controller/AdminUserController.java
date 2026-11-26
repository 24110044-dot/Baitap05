package com.example.bt05.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bt05.dto.UserForm;
import com.example.bt05.entity.User;
import com.example.bt05.service.AccountService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

	private final AccountService accounts;

	public AdminUserController(AccountService accounts) {
		this.accounts = accounts;
	}

	@GetMapping
	public String list(@RequestParam(defaultValue = "") String keyword,
			@RequestParam(defaultValue = "") String role,
			@RequestParam(required = false) Boolean status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, Model model) {
		Page<User> result = accounts.search(keyword, role, status, page, size);
		model.addAttribute("users", result);
		model.addAttribute("keyword", keyword);
		model.addAttribute("role", role);
		model.addAttribute("status", status);
		model.addAttribute("size", result.getSize());
		model.addAttribute("pageTitle", "Người dùng");
		return "admin/users/list";
	}

	@GetMapping("/create")
	public String create(Model model) {
		model.addAttribute("form", new UserForm());
		model.addAttribute("formAction", "/admin/users/store");
		model.addAttribute("pageTitle", "Thêm người dùng");
		model.addAttribute("creating", true);
		return "admin/users/form";
	}

	@PostMapping("/store")
	public String store(@Valid @ModelAttribute("form") UserForm form, BindingResult result,
			Model model, RedirectAttributes redirect) {
		if (result.hasErrors()) {
			prepareForm(model, "/admin/users/store", "Thêm người dùng", true);
			return "admin/users/form";
		}
		try {
			accounts.save(form, null);
			redirect.addFlashAttribute("success", "Đã tạo người dùng.");
			return "redirect:/admin/users";
		} catch (IllegalArgumentException exception) {
			model.addAttribute("error", exception.getMessage());
			prepareForm(model, "/admin/users/store", "Thêm người dùng", true);
			return "admin/users/form";
		}
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Integer id, Model model) {
		User user = accounts.get(id);
		UserForm form = new UserForm();
		form.setUsername(user.getUsername());
		form.setEmail(user.getEmail());
		form.setFullName(user.getFullName());
		form.setPhone(user.getPhone());
		form.setRole(user.getRole());
		form.setStatus(user.isStatus());
		model.addAttribute("form", form);
		prepareForm(model, "/admin/users/update/" + id, "Chỉnh sửa người dùng", false);
		return "admin/users/form";
	}

	@PostMapping("/update/{id}")
	public String update(@PathVariable Integer id, @Valid @ModelAttribute("form") UserForm form,
			BindingResult result, Model model, RedirectAttributes redirect) {
		if (result.hasErrors()) {
			prepareForm(model, "/admin/users/update/" + id, "Chỉnh sửa người dùng", false);
			return "admin/users/form";
		}
		try {
			accounts.save(form, id);
			redirect.addFlashAttribute("success", "Đã cập nhật người dùng.");
			return "redirect:/admin/users";
		} catch (IllegalArgumentException exception) {
			model.addAttribute("error", exception.getMessage());
			prepareForm(model, "/admin/users/update/" + id, "Chỉnh sửa người dùng", false);
			return "admin/users/form";
		}
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
		try {
			accounts.delete(id);
			redirect.addFlashAttribute("success", "Đã xóa người dùng.");
		} catch (IllegalArgumentException exception) {
			redirect.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin/users";
	}

	private void prepareForm(Model model, String action, String title, boolean creating) {
		model.addAttribute("formAction", action);
		model.addAttribute("pageTitle", title);
		model.addAttribute("creating", creating);
	}
}
