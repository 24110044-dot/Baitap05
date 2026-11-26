package com.example.bt05;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.example.bt05.entity.User;
import com.example.bt05.entity.Category;
import com.example.bt05.repository.CategoryRepository;
import com.example.bt05.repository.UserRepository;
import org.springframework.data.domain.PageRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class JspRenderingTests {

	@Value("${local.server.port}")
	private int port;

	@Value("${app.uploads.directory}")
	private String uploadsDirectory;

	@Autowired
	private UserRepository users;

	@Autowired
	private CategoryRepository categories;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Path uploadedTestImage;

	@AfterEach
	void cleanUploadedTestImage() throws IOException {
		if (uploadedTestImage != null) {
			Files.deleteIfExists(uploadedTestImage);
			uploadedTestImage = null;
		}
	}

	@Test
	void rendersLoginAndRegisterJspWithJstl() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> loginResponse = send(client, "GET", "/login", null);
		HttpResponse<String> registerResponse = send(client, "GET", "/register", null);
		assertThat(loginResponse.statusCode()).isEqualTo(200);
		assertThat(registerResponse.statusCode()).isEqualTo(200);
		assertThat(loginResponse.headers().firstValue("content-type").orElse(""))
				.containsIgnoringCase("charset=UTF-8");
		assertThat(registerResponse.headers().firstValue("content-type").orElse(""))
				.containsIgnoringCase("charset=UTF-8");
		String login = loginResponse.body();
		String register = registerResponse.body();
		String stylesheet = get(client, "/css/app.css");

		assertThat(login).contains("<title>Đăng nhập</title>", "name=\"username\"", "name=\"password\"");
		assertThat(register).contains("<title>Tạo tài khoản</title>", "name=\"confirmPassword\"",
				"Tài khoản đăng ký mới được tạo với quyền USER.");
		assertThat(login).doesNotContain("jakarta.tags.core");
		assertThat(stylesheet).contains("--cyber-purple", "--cyber-teal", "@media (max-width: 640px)");
	}

	@Test
	void registeredUserCanLoginButCannotOpenAdminAndCanLogout() throws Exception {
		HttpClient client = clientWithCookies();
		HttpResponse<String> registerPage = send(client, "GET", "/register", null);
		assertThat(registerPage.statusCode()).isEqualTo(200);
		HttpResponse<String> registration = send(client, "POST", "/register", form(Map.of(
				"_csrf", csrf(registerPage.body()),
				"username", "visitor",
				"password", "Password123!",
				"confirmPassword", "Password123!",
				"email", "visitor@example.com",
				"fullName", "Test Visitor")));
		assertThat(registration.statusCode()).isEqualTo(302);
		assertThat(registration.headers().firstValue("location").orElse("")).endsWith("/login");

		HttpResponse<String> loginPage = send(client, "GET", "/login", null);
		HttpResponse<String> failedLogin = send(client, "POST", "/login", form(Map.of(
				"_csrf", csrf(loginPage.body()),
				"username", "visitor",
				"password", "wrong-password")));
		assertThat(failedLogin.statusCode()).isEqualTo(302);
		assertThat(failedLogin.headers().firstValue("location").orElse("")).contains("/login?error");

		loginPage = send(client, "GET", "/login", null);
		HttpResponse<String> successfulLogin = send(client, "POST", "/login", form(Map.of(
				"_csrf", csrf(loginPage.body()),
				"username", "visitor",
				"password", "Password123!")));
		assertThat(successfulLogin.statusCode()).isEqualTo(302);
		assertThat(successfulLogin.headers().firstValue("location").orElse("")).endsWith("/user/dashboard");

		HttpResponse<String> userPage = send(client, "GET", "/user/dashboard", null);
		assertThat(userPage.statusCode()).isEqualTo(200);
		assertThat(userPage.body()).contains("Trang dành cho User");
		assertThat(send(client, "GET", "/admin/categories", null).statusCode()).isEqualTo(403);

		HttpResponse<String> logout = send(client, "POST", "/logout", form(Map.of(
				"_csrf", csrf(userPage.body()))));
		assertThat(logout.statusCode()).isEqualTo(302);
		assertThat(logout.headers().firstValue("location").orElse("")).contains("/login?logout");
		assertThat(send(client, "GET", "/user/dashboard", null).statusCode()).isEqualTo(302);
	}

	@Test
	void activeAdminCanOpenAdminPagesAndLockedAccountCannotLogin() throws Exception {
		User admin = new User();
		admin.setUsername("httpadmin");
		admin.setEmail("httpadmin@example.com");
		admin.setPassword(passwordEncoder.encode("AdminPassword123!"));
		admin.setRole("ADMIN");
		admin.setStatus(true);
		users.saveAndFlush(admin);

		User locked = new User();
		locked.setUsername("lockeduser");
		locked.setEmail("locked@example.com");
		locked.setPassword(passwordEncoder.encode("LockedPassword123!"));
		locked.setRole("USER");
		locked.setStatus(false);
		users.saveAndFlush(locked);

		HttpClient lockedClient = clientWithCookies();
		HttpResponse<String> lockedLoginPage = send(lockedClient, "GET", "/login", null);
		HttpResponse<String> lockedLogin = send(lockedClient, "POST", "/login", form(Map.of(
				"_csrf", csrf(lockedLoginPage.body()),
				"username", "lockeduser",
				"password", "LockedPassword123!")));
		assertThat(lockedLogin.statusCode()).isEqualTo(302);
		assertThat(lockedLogin.headers().firstValue("location").orElse("")).contains("/login?error");

		HttpClient adminClient = clientWithCookies();
		HttpResponse<String> loginPage = send(adminClient, "GET", "/login", null);
		HttpResponse<String> login = send(adminClient, "POST", "/login", form(Map.of(
				"_csrf", csrf(loginPage.body()),
				"username", "httpadmin",
				"password", "AdminPassword123!")));
		assertThat(login.statusCode()).isEqualTo(302);
		assertThat(login.headers().firstValue("location").orElse("")).endsWith("/admin/dashboard");

		assertThat(send(adminClient, "GET", "/admin/dashboard", null).body()).contains("Admin Dashboard");
		String categoryListPage = send(adminClient, "GET", "/admin/categories", null).body();
		assertThat(categoryListPage).contains("Danh mục", "Người dùng", "Quyền quản trị đang bật", "Đăng xuất");
		assertThat(send(adminClient, "GET", "/admin/users", null).body()).contains("httpadmin");

		HttpResponse<String> categoryForm = send(adminClient, "GET", "/admin/categories/create", null);
		assertThat(categoryForm.statusCode()).isEqualTo(200);
		assertThat(categoryForm.body()).contains("Tên danh mục", "enctype=\"multipart/form-data\"", "name=\"imageFile\"");
		byte[] imageBytes = tinyPng();
		HttpResponse<String> categoryCreate = sendMultipart(adminClient, "/admin/categories/store", Map.of(
				"_csrf", csrf(categoryForm.body()),
				"categoryName", "CRUD category",
				"images", "",
				"status", "true"), "imageFile", "category.png", "image/png", imageBytes);
		assertThat(categoryCreate.statusCode()).isEqualTo(302);
		Category category = categories.findByCategoryNameContainingIgnoreCase("CRUD category",
				PageRequest.of(0, 5)).getContent().get(0);
		assertThat(category.getImages()).startsWith("/uploads/categories/");
		uploadedTestImage = Path.of(uploadsDirectory).toAbsolutePath().normalize()
				.resolve("categories")
				.resolve(category.getImages().substring("/uploads/categories/".length()));
		HttpResponse<byte[]> uploadedImage = sendBytes(adminClient, category.getImages());
		assertThat(uploadedImage.statusCode()).isEqualTo(200);
		assertThat(uploadedImage.headers().firstValue("content-type").orElse("")).contains("image/png");
		assertThat(uploadedImage.body()).isEqualTo(imageBytes);

		HttpResponse<String> categoryEditPage = send(adminClient, "GET",
				"/admin/categories/edit/" + category.getCategoryId(), null);
		assertThat(categoryEditPage.statusCode()).isEqualTo(200);
		assertThat(categoryEditPage.body()).contains(category.getImages());
		HttpResponse<String> categoryUpdate = send(adminClient, "POST",
				"/admin/categories/update/" + category.getCategoryId(), form(Map.of(
						"_csrf", csrf(categoryEditPage.body()),
						"categoryName", "CRUD category updated",
						"images", category.getImages(),
						"status", "true")));
		assertThat(categoryUpdate.statusCode()).isEqualTo(302);
		assertThat(categories.findById(category.getCategoryId()).orElseThrow().getCategoryName())
				.isEqualTo("CRUD category updated");
		HttpResponse<String> categoryList = send(adminClient, "GET", "/admin/categories", null);
		assertThat(categoryList.body()).contains("category-icon-image", category.getImages());
		HttpResponse<String> categoryDelete = send(adminClient, "POST",
				"/admin/categories/delete/" + category.getCategoryId(),
				form(Map.of("_csrf", csrf(categoryList.body()))));
		assertThat(categoryDelete.statusCode()).isEqualTo(302);
		assertThat(categories.existsById(category.getCategoryId())).isFalse();

		HttpResponse<String> linkedCategoryForm = send(adminClient, "GET", "/admin/categories/create", null);
		HttpResponse<String> linkedCategoryCreate = send(adminClient, "POST", "/admin/categories/store",
				form(Map.of(
						"_csrf", csrf(linkedCategoryForm.body()),
						"categoryName", "URL image category",
						"images", "https://example.com/category.png",
						"status", "true")));
		assertThat(linkedCategoryCreate.statusCode()).isEqualTo(302);
		Category linkedCategory = categories.findByCategoryNameContainingIgnoreCase("URL image category",
				PageRequest.of(0, 5)).getContent().get(0);
		assertThat(linkedCategory.getImages()).isEqualTo("https://example.com/category.png");
		HttpResponse<String> categoriesAfterUrlCreate = send(adminClient, "GET", "/admin/categories", null);
		HttpResponse<String> linkedCategoryDelete = send(adminClient, "POST",
				"/admin/categories/delete/" + linkedCategory.getCategoryId(),
				form(Map.of("_csrf", csrf(categoriesAfterUrlCreate.body()))));
		assertThat(linkedCategoryDelete.statusCode()).isEqualTo(302);

		HttpResponse<String> userForm = send(adminClient, "GET", "/admin/users/create", null);
		assertThat(userForm.statusCode()).isEqualTo(200);
		assertThat(userForm.body()).contains("Mật khẩu");
		HttpResponse<String> userCreate = send(adminClient, "POST", "/admin/users/store", form(Map.of(
				"_csrf", csrf(userForm.body()),
				"username", "cruduser",
				"email", "cruduser@example.com",
				"fullName", "CRUD User",
				"phone", "",
				"password", "CrudPassword123!",
				"role", "USER",
				"status", "true")));
		assertThat(userCreate.statusCode()).isEqualTo(302);
		User createdUser = users.findByUsernameIgnoreCase("cruduser").orElseThrow();
		String originalPassword = createdUser.getPassword();
		HttpResponse<String> userEditPage = send(adminClient, "GET",
				"/admin/users/edit/" + createdUser.getUserId(), null);
		assertThat(userEditPage.statusCode()).isEqualTo(200);
		HttpResponse<String> userUpdate = send(adminClient, "POST",
				"/admin/users/update/" + createdUser.getUserId(), form(Map.of(
						"_csrf", csrf(userEditPage.body()),
						"username", "cruduser",
						"email", "cruduser@example.com",
						"fullName", "Updated CRUD User",
						"phone", "",
						"password", "",
						"role", "USER",
						"status", "true")));
		assertThat(userUpdate.statusCode()).isEqualTo(302);
		User updatedUser = users.findById(createdUser.getUserId()).orElseThrow();
		assertThat(updatedUser.getPassword()).isEqualTo(originalPassword);
		assertThat(updatedUser.getFullName()).isEqualTo("Updated CRUD User");
		HttpResponse<String> userDelete = send(adminClient, "POST",
				"/admin/users/delete/" + createdUser.getUserId(),
				form(Map.of("_csrf", csrf(userEditPage.body()))));
		assertThat(userDelete.statusCode()).isEqualTo(302);
		assertThat(users.existsById(createdUser.getUserId())).isFalse();
	}

	private String get(HttpClient client, String path) throws Exception {
		HttpResponse<String> response = send(client, "GET", path, null);
		assertThat(response.statusCode()).as("HTTP status for " + path + ", location="
				+ response.headers().firstValue("location").orElse("none")).isEqualTo(200);
		return response.body();
	}

	private HttpClient clientWithCookies() {
		return HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
	}

	private HttpResponse<String> send(HttpClient client, String method, String path, String body) throws Exception {
		HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
		if (body == null) {
			request.GET();
		} else {
			request.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(body));
		}
		return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<byte[]> sendBytes(HttpClient client, String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
		return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
	}

	private HttpResponse<String> sendMultipart(HttpClient client, String path, Map<String, String> fields,
			String fileField, String fileName, String contentType, byte[] fileBytes) throws Exception {
		String boundary = "Boundary-" + UUID.randomUUID();
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		for (Map.Entry<String, String> field : fields.entrySet()) {
			body.write(("--" + boundary + "\r\n"
					+ "Content-Disposition: form-data; name=\"" + field.getKey() + "\"\r\n\r\n"
					+ field.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
		}
		body.write(("--" + boundary + "\r\n"
				+ "Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\"" + fileName + "\"\r\n"
				+ "Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
		body.write(fileBytes);
		body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.header("Content-Type", "multipart/form-data; boundary=" + boundary)
				.POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
				.build();
		return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
	}

	private byte[] tinyPng() throws IOException {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		assertThat(ImageIO.write(image, "png", output)).isTrue();
		return output.toByteArray();
	}

	private String csrf(String html) {
		Matcher matcher = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(html);
		assertThat(matcher.find()).isTrue();
		return matcher.group(1);
	}

	private String form(Map<String, String> fields) {
		return fields.entrySet().stream()
				.map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
						+ URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
				.collect(java.util.stream.Collectors.joining("&"));
	}
}
