package com.example.bt05.service;

import java.nio.charset.StandardCharsets;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bt05.dto.RegisterForm;
import com.example.bt05.dto.UserForm;
import com.example.bt05.entity.User;
import com.example.bt05.repository.UserRepository;

@Service
public class AccountService {

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;

	public AccountService(UserRepository users, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void register(RegisterForm form) {
		if (!form.getPassword().equals(form.getConfirmPassword())) {
			throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
		}
		validatePassword(form.getPassword());
		if (users.existsByUsernameIgnoreCase(form.getUsername().trim())) {
			throw new IllegalArgumentException("Username đã được sử dụng.");
		}
		if (users.existsByEmailIgnoreCase(form.getEmail().trim())) {
			throw new IllegalArgumentException("Email đã được sử dụng.");
		}
		User user = new User();
		user.setUsername(form.getUsername().trim());
		user.setPassword(passwordEncoder.encode(form.getPassword()));
		user.setEmail(form.getEmail().trim());
		user.setFullName(blankToNull(form.getFullName()));
		user.setPhone(blankToNull(form.getPhone()));
		user.setRole("USER");
		user.setStatus(true);
		saveWithDuplicateMessage(user);
	}

	@Transactional(readOnly = true)
	public Page<User> search(String keyword, String role, Boolean status, int page, int size) {
		return users.search(keyword == null ? "" : keyword.trim(), role == null ? "" : role,
				status, PageRequest.of(Math.max(page, 0), normalizeSize(size),
						Sort.by("userId").descending()));
	}

	@Transactional(readOnly = true)
	public User get(Integer id) {
		return users.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));
	}

	@Transactional
	public User save(UserForm form, Integer id) {
		User user = id == null ? new User() : get(id);
		if (id == null && (form.getPassword() == null || form.getPassword().isBlank())) {
			throw new IllegalArgumentException("Mật khẩu là bắt buộc khi tạo tài khoản.");
		}
		if (form.getPassword() != null && !form.getPassword().isBlank()) {
			validatePassword(form.getPassword());
		}
		String username = form.getUsername().trim();
		String email = form.getEmail().trim();
		if (id == null ? users.existsByUsernameIgnoreCase(username)
				: users.existsByUsernameIgnoreCaseAndUserIdNot(username, id)) {
			throw new IllegalArgumentException("Username đã được sử dụng.");
		}
		if (id == null ? users.existsByEmailIgnoreCase(email)
				: users.existsByEmailIgnoreCaseAndUserIdNot(email, id)) {
			throw new IllegalArgumentException("Email đã được sử dụng.");
		}
		boolean removesActiveAdmin = "ADMIN".equals(user.getRole()) && user.isStatus()
				&& (!"ADMIN".equals(form.getRole()) || !form.isStatus());
		if (removesActiveAdmin && users.countByRoleAndStatusTrue("ADMIN") <= 1) {
			throw new IllegalArgumentException("Không thể vô hiệu hóa hoặc hạ quyền ADMIN đang hoạt động cuối cùng.");
		}
		user.setUsername(username);
		user.setEmail(email);
		user.setFullName(blankToNull(form.getFullName()));
		user.setPhone(blankToNull(form.getPhone()));
		user.setRole(form.getRole());
		user.setStatus(form.isStatus());
		if (form.getPassword() != null && !form.getPassword().isBlank()) {
			user.setPassword(passwordEncoder.encode(form.getPassword()));
		}
		return saveWithDuplicateMessage(user);
	}

	@Transactional
	public void delete(Integer id) {
		User user = get(id);
		if ("ADMIN".equals(user.getRole()) && user.isStatus()
				&& users.countByRoleAndStatusTrue("ADMIN") <= 1) {
			throw new IllegalArgumentException("Không thể xóa ADMIN đang hoạt động cuối cùng.");
		}
		users.delete(user);
	}

	private User saveWithDuplicateMessage(User user) {
		try {
			return users.saveAndFlush(user);
		} catch (DataIntegrityViolationException exception) {
			throw new IllegalArgumentException("Username hoặc email đã tồn tại.", exception);
		}
	}

	private int normalizeSize(int size) {
		return size < 5 ? 5 : Math.min(size, 50);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private void validatePassword(String password) {
		int byteLength = password.getBytes(StandardCharsets.UTF_8).length;
		if (password.length() < 8 || byteLength > 72) {
			throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự và tối đa 72 byte UTF-8.");
		}
	}
}
