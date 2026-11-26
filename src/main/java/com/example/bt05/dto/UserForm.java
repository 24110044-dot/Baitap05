package com.example.bt05.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserForm {

	@NotBlank(message = "Username không được để trống.")
	@Size(max = 50)
	private String username;

	@Email(message = "Email không hợp lệ.")
	@NotBlank(message = "Email không được để trống.")
	@Size(max = 100)
	private String email;

	@Size(max = 100)
	private String fullName;

	@Pattern(regexp = "^(?:$|[0-9+()\\-\\s]{7,15})$", message = "Số điện thoại không hợp lệ.")
	private String phone;

	@Pattern(regexp = "^(?:$|.{8,100})$", message = "Mật khẩu mới phải có từ 8 đến 100 ký tự.")
	private String password;

	@Pattern(regexp = "ADMIN|USER", message = "Role không hợp lệ.")
	private String role = "USER";

	private boolean status = true;

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }
	public boolean isStatus() { return status; }
	public void setStatus(boolean status) { this.status = status; }
}
