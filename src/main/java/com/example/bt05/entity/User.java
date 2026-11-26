package com.example.bt05.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "userId")
	private Integer userId;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "username", nullable = false, unique = true, length = 50)
	private String username;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "password", nullable = false, length = 255)
	private String password;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "email", nullable = false, unique = true, length = 100)
	private String email;

	@Column(name = "fullName", length = 100)
	private String fullName;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "phone", length = 15)
	private String phone;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "role", nullable = false, length = 20)
	private String role = "USER";

	@Column(name = "status", nullable = false)
	private boolean status = true;

	@Column(name = "createdAt", nullable = false, insertable = false, updatable = false)
	private LocalDateTime createdAt;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
