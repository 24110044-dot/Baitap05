package com.example.bt05.config;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.bt05.entity.User;
import com.example.bt05.repository.UserRepository;

@Component
public class AdminBootstrap implements ApplicationRunner {

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final boolean enabled;
	private final String username;
	private final String email;
	private final String password;

	public AdminBootstrap(UserRepository users, PasswordEncoder passwordEncoder,
			@Value("${app.bootstrap-admin.enabled:false}") boolean enabled,
			@Value("${app.bootstrap-admin.username:}") String username,
			@Value("${app.bootstrap-admin.email:}") String email,
			@Value("${app.bootstrap-admin.password:}") String password) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.enabled = enabled;
		this.username = username;
		this.email = email;
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!enabled || users.countByRole("ADMIN") > 0) {
			return;
		}
		if (username.isBlank() || email.isBlank() || password.isBlank()
				|| username.length() > 50 || email.length() > 100
				|| password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new IllegalStateException("Initial admin bootstrap is enabled but its credentials are invalid.");
		}
		if (users.existsByUsernameIgnoreCase(username) || users.existsByEmailIgnoreCase(email)) {
			throw new IllegalStateException("Initial admin username or email is already in use.");
		}
		User admin = new User();
		admin.setUsername(username);
		admin.setEmail(email);
		admin.setPassword(passwordEncoder.encode(password));
		admin.setRole("ADMIN");
		admin.setStatus(true);
		users.saveAndFlush(admin);
	}
}
