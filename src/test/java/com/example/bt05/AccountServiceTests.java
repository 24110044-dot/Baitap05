package com.example.bt05;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.DefaultApplicationArguments;

import com.example.bt05.config.AdminBootstrap;
import com.example.bt05.dto.CategoryForm;
import com.example.bt05.dto.RegisterForm;
import com.example.bt05.dto.UserForm;
import com.example.bt05.entity.Category;
import com.example.bt05.entity.User;
import com.example.bt05.repository.UserRepository;
import com.example.bt05.service.AccountService;
import com.example.bt05.service.CategoryService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccountServiceTests {

	@Autowired
	private AccountService accounts;

	@Autowired
	private CategoryService categories;

	@Autowired
	private UserRepository users;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	@Transactional
	void registrationHashesPasswordAndAlwaysCreatesActiveUserRole() {
		RegisterForm form = registration("newuser", "new@example.com");
		accounts.register(form);

		User saved = users.findByUsernameIgnoreCase("newuser").orElseThrow();
		assertThat(saved.getPassword()).isNotEqualTo(form.getPassword());
		assertThat(passwordEncoder.matches(form.getPassword(), saved.getPassword())).isTrue();
		assertThat(saved.getRole()).isEqualTo("USER");
		assertThat(saved.isStatus()).isTrue();
	}

	@Test
	@Transactional
	void registrationRejectsMismatchedPasswordsAndDuplicateIdentityFields() {
		RegisterForm first = registration("duplicate", "first@example.com");
		accounts.register(first);

		RegisterForm duplicateUsername = registration("duplicate", "second@example.com");
		assertThatThrownBy(() -> accounts.register(duplicateUsername))
				.hasMessageContaining("Username");

		RegisterForm duplicateEmail = registration("different", "first@example.com");
		assertThatThrownBy(() -> accounts.register(duplicateEmail))
				.hasMessageContaining("Email");

		RegisterForm mismatched = registration("other", "other@example.com");
		mismatched.setConfirmPassword("not-the-password");
		assertThatThrownBy(() -> accounts.register(mismatched))
				.hasMessageContaining("không khớp");
	}

	@Test
	@Transactional
	void adminUpdateKeepsOldPasswordAndProtectsLastActiveAdministrator() {
		User admin = new User();
		admin.setUsername("only-admin");
		admin.setEmail("admin@example.com");
		admin.setPassword(passwordEncoder.encode("AdminPassword1!"));
		admin.setRole("ADMIN");
		admin.setStatus(true);
		users.saveAndFlush(admin);
		String originalPassword = admin.getPassword();

		UserForm update = new UserForm();
		update.setUsername(admin.getUsername());
		update.setEmail(admin.getEmail());
		update.setRole("ADMIN");
		update.setStatus(true);
		accounts.save(update, admin.getUserId());

		assertThat(users.findById(admin.getUserId()).orElseThrow().getPassword()).isEqualTo(originalPassword);

		update.setStatus(false);
		assertThatThrownBy(() -> accounts.save(update, admin.getUserId()))
				.hasMessageContaining("cuối cùng");
		assertThatThrownBy(() -> accounts.delete(admin.getUserId()))
				.hasMessageContaining("cuối cùng");
	}

	@Test
	@Transactional
	void categorySearchUsesDatabasePageAndUpdatePreservesCreatedAt() {
		for (int index = 0; index < 6; index++) {
			CategoryForm form = new CategoryForm();
			form.setCategoryName("Test category " + index);
			categories.save(form, null);
		}

		assertThat(categories.search("TEST CATEGORY", 0, 5).getTotalElements()).isEqualTo(6);
		assertThat(categories.search("TEST CATEGORY", 0, 5).getContent()).hasSize(5);

		Category category = categories.search("Test category 0", 0, 5).getContent().get(0);
		Category persisted = categories.get(category.getCategoryId());
		var createdAt = persisted.getCreatedAt();
		CategoryForm edit = new CategoryForm();
		edit.setCategoryName("Renamed category");
		edit.setStatus(true);
		categories.save(edit, persisted.getCategoryId());
		assertThat(categories.get(persisted.getCategoryId()).getCreatedAt()).isEqualTo(createdAt);
	}

	@Test
	@Transactional
	void userSearchAppliesKeywordRoleStatusAndDatabasePagination() {
		for (int index = 0; index < 6; index++) {
			User user = new User();
			user.setUsername("searchuser" + index);
			user.setEmail("search" + index + "@example.com");
			user.setFullName("Search Person " + index);
			user.setPassword(passwordEncoder.encode("Password123!"));
			user.setRole(index == 0 ? "ADMIN" : "USER");
			user.setStatus(index != 5);
			users.save(user);
		}

		var page = accounts.search("SEARCH PERSON", "USER", true, 0, 5);
		assertThat(page.getTotalElements()).isEqualTo(4);
		assertThat(page.getContent()).hasSize(4);
	}

	@Test
	@Transactional
	void optInBootstrapCreatesOnlyOneBcryptAdministrator() {
		String password = "BootstrapPassword123!";
		AdminBootstrap bootstrap = new AdminBootstrap(users, passwordEncoder, true,
				"bootstrap-admin", "bootstrap@example.com", password);

		bootstrap.run(new DefaultApplicationArguments(new String[0]));
		String originalPasswordHash = users.findByUsernameIgnoreCase("bootstrap-admin").orElseThrow().getPassword();
		bootstrap.run(new DefaultApplicationArguments(new String[0]));

		User admin = users.findByUsernameIgnoreCase("bootstrap-admin").orElseThrow();
		assertThat(admin.getRole()).isEqualTo("ADMIN");
		assertThat(admin.isStatus()).isTrue();
		assertThat(passwordEncoder.matches(password, admin.getPassword())).isTrue();
		assertThat(admin.getPassword()).isEqualTo(originalPasswordHash);
		assertThat(users.countByRoleAndStatusTrue("ADMIN")).isEqualTo(1);
	}

	@Test
	@Transactional
	void bootstrapDoesNotCreateAnotherAdminWhenAnExistingAdminIsLocked() {
		User lockedAdmin = new User();
		lockedAdmin.setUsername("locked-admin");
		lockedAdmin.setEmail("locked-admin@example.com");
		lockedAdmin.setPassword(passwordEncoder.encode("ExistingPassword123!"));
		lockedAdmin.setRole("ADMIN");
		lockedAdmin.setStatus(false);
		users.saveAndFlush(lockedAdmin);
		String existingPasswordHash = lockedAdmin.getPassword();

		AdminBootstrap bootstrap = new AdminBootstrap(users, passwordEncoder, true,
				"new-bootstrap-admin", "new-bootstrap@example.com", "BootstrapPassword123!");
		bootstrap.run(new DefaultApplicationArguments(new String[0]));

		assertThat(users.findByUsernameIgnoreCase("locked-admin").orElseThrow().getPassword())
				.isEqualTo(existingPasswordHash);
		assertThat(users.findByUsernameIgnoreCase("new-bootstrap-admin")).isEmpty();
		assertThat(users.countByRole("ADMIN")).isEqualTo(1);
	}

	private RegisterForm registration(String username, String email) {
		RegisterForm form = new RegisterForm();
		form.setUsername(username);
		form.setPassword("Password123!");
		form.setConfirmPassword("Password123!");
		form.setEmail(email);
		return form;
	}
}
