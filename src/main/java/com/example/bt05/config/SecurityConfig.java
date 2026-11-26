package com.example.bt05.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.example.bt05.repository.UserRepository;

import jakarta.servlet.DispatcherType;

@Configuration
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(UserRepository users) {
		return username -> users.findByUsernameIgnoreCase(username)
				.map(user -> org.springframework.security.core.userdetails.User
						.withUsername(user.getUsername())
						.password(user.getPassword())
						.roles(user.getRole())
						.disabled(!user.isStatus())
						.build())
				.orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
						"Không tìm thấy tài khoản."));
	}

	@Bean
	AuthenticationSuccessHandler roleRedirectHandler() {
		return (request, response, authentication) -> {
			String destination = authentication.getAuthorities().stream()
					.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))
							? "/admin/dashboard" : "/user/dashboard";
			response.sendRedirect(request.getContextPath() + destination);
		};
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
			AuthenticationSuccessHandler roleRedirectHandler) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
						.requestMatchers("/", "/login", "/register", "/error", "/css/**", "/js/**", "/images/**")
						.permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/user/**").hasRole("USER")
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/login")
						.successHandler(roleRedirectHandler)
						.failureUrl("/login?error")
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("JSESSIONID")
						.logoutSuccessUrl("/login?logout")
						.permitAll())
				.sessionManagement(session -> session.sessionFixation(fixation -> fixation.migrateSession()));
		return http.build();
	}
}
