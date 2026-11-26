package com.example.bt05.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterForm {

	@NotBlank
	@Size(max = 50)
	private String username;

	@NotBlank
	@Size(min = 8, max = 100)
	private String password;

	@NotBlank
	private String confirmPassword;

	@NotBlank
	@Email
	@Size(max = 100)
	private String email;

	@Size(max = 100)
	private String fullName;

	@Pattern(regexp = "^(?:|[0-9+()\\-\\s]{7,15})$")
	private String phone;

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public String getConfirmPassword() { return confirmPassword; }
	public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
}
