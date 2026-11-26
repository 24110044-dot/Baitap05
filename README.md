# Báo cáo kiểm tra Category Management

**Ngày kiểm tra:** 26/11/2026

**Phạm vi:** Bốn yêu cầu bài tập: cấu hình JSP/JSTL, tìm kiếm và phân trang,
đăng ký/đăng nhập/đăng xuất, CRUD Category/User có phân quyền Admin.


## 1. Công nghệ và cấu hình

- Java 17, Spring Boot 4.1.2, Maven Wrapper, đóng gói WAR.
- Spring MVC, JSP/JSTL, Spring Security, Spring Data JPA/Hibernate.
- SQL Server `CategoryManagementDB`; `spring.jpa.hibernate.ddl-auto=validate`.
- JSP view prefix/suffix: `/WEB-INF/views/` và `.jsp`.
- Maven source encoding và servlet request/response encoding cấu hình UTF-8.
- Test thông thường sử dụng H2 riêng; test SQL Server thật là test opt-in.

## 2. Đánh giá theo yêu cầu

| Yêu cầu | Kết quả | Đối chiếu code và test |
|---|---|---|
| JSP/JSTL với Spring Boot 4.1.2 | **Đạt** | `pom.xml` có Jasper, JSTL API/implementation và WAR packaging. HTTP tests khởi động Tomcat, render Login/Register và trang Admin, kiểm tra nội dung tiếng Việt cùng `charset=UTF-8`. |
| Tìm kiếm và phân trang Category/User | **Đạt theo code; H2 đã kiểm thử** | Category tìm theo tên và dùng `Pageable`. User tìm kiếm theo từ khóa, lọc role/status và dùng `Pageable`. Service tests xác nhận tìm kiếm, lọc và giới hạn số kết quả theo trang. |
| Đăng ký, Login, Logout bằng Spring Security | **Đạt** | Đăng ký kiểm tra dữ liệu, trùng username/email và mật khẩu xác nhận; mật khẩu lưu BCrypt, tài khoản mới có role USER. Login xác thực BCrypt, chặn tài khoản inactive và chuyển hướng theo role. Logout POST xóa Authentication/session. HTTP tests kiểm tra luồng đăng ký/đăng nhập/đăng xuất. |
| CRUD Category/User chỉ dành cho Admin | **Đạt theo code và HTTP tests** | `/admin/**` yêu cầu role ADMIN. Form/service có validation bắt buộc, độ dài, định dạng và tính duy nhất. HTTP tests thực hiện tạo/sửa/xóa Category và User; xác nhận User bị từ chối tại `/admin/categories`. Service test kiểm tra giữ mật khẩu cũ khi bỏ trống mật khẩu mới và bảo vệ Admin hoạt động cuối cùng. |

### Spring Security và CSRF

- CSRF không bị tắt trong `SecurityConfig`.
- Form POST gửi CSRF token; HTTP test mới xác nhận POST đăng ký thiếu token trả
  **403** và không tạo tài khoản.
- Session fixation protection được bật. Logout invalidate session, xóa Authentication
  và cookie `JSESSIONID`; test xác nhận sau logout không còn truy cập được trang User.
- Quy tắc `/admin/**` áp dụng chung cho cả Category và User; User không có quyền
  truy cập nhóm URL này.

## 3. URL và HTTP method

| Method | URL | Chức năng / quyền |
|---|---|---|
| GET | `/` | Điều hướng theo trạng thái đăng nhập và role |
| GET | `/login` | Form Login |
| POST | `/login` | Spring Security xác thực |
| GET | `/register` | Form đăng ký |
| POST | `/register` | Tạo tài khoản USER |
| POST | `/logout` | Đăng xuất, yêu cầu CSRF |
| GET | `/admin/dashboard` | Dashboard Admin |
| GET | `/user/dashboard` | Trang dành cho User |
| GET | `/admin/categories` | Danh sách, tìm kiếm, phân trang Category |
| GET | `/admin/categories/create` | Form tạo Category |
| POST | `/admin/categories/store` | Tạo Category |
| GET | `/admin/categories/edit/{id}` | Form sửa Category |
| POST | `/admin/categories/update/{id}` | Cập nhật Category |
| POST | `/admin/categories/delete/{id}` | Xóa Category |
| GET | `/admin/users` | Danh sách, tìm kiếm/lọc, phân trang User |
| GET | `/admin/users/create` | Form tạo User |
| POST | `/admin/users/store` | Tạo User |
| GET | `/admin/users/edit/{id}` | Form sửa User |
| POST | `/admin/users/update/{id}` | Cập nhật User |
| POST | `/admin/users/delete/{id}` | Xóa User |

Category list nhận `keyword`, `page`, `size`. User list nhận thêm `role`, `status`.
Các thay đổi dữ liệu dùng POST; form POST được bảo vệ bằng CSRF.

## 4. Kết quả kiểm thử

### H2 và đóng gói

Đã chạy:

```powershell
.\mvnw.cmd clean test package
```

**Kết quả: BUILD SUCCESS.** Tổng cộng 12 test: **11 pass, 0 fail, 1 skipped**.
Test bị skip là `SqlServerJdbcIntegrationTest` vì cần bật riêng. Các test H2 bao gồm:

- Tìm kiếm/phân trang Category; tìm kiếm/lọc/phân trang User.
- BCrypt, đăng ký không cấp ADMIN, trùng username/email và mật khẩu xác nhận.
- Tài khoản inactive không đăng nhập được; chuyển hướng Login theo role.
- USER bị từ chối truy cập Admin; CRUD Category/User qua HTTP.
- Render JSP/JSTL, UTF-8, logout và từ chối POST thiếu CSRF.
- Không xóa hoặc vô hiệu hóa Admin hoạt động cuối cùng.

### SQL Server thật

Đã chạy riêng test JDBC bằng Windows Authentication:

```powershell
$authDir = Join-Path $env:USERPROFILE '.m2\repository\com\microsoft\sqlserver\mssql-jdbc_auth\13.4.0.x64'
$env:PATH = "$authDir;$env:PATH"
.\mvnw.cmd '-Dsqlserver.integration=true' '-Dtest=SqlServerJdbcIntegrationTest' test
```

**Kết quả: BUILD SUCCESS, 1 test pass, 0 fail, 0 skipped.** Log xác nhận kết nối
Microsoft JDBC Driver 13.4 đến `CategoryManagementDB`, SQL Server 16.0, schema `dbo`.
Test chạy `SELECT 1`, đếm dòng hai bảng hiện có bằng JDBC, đối chiếu số lượng qua JPA
và thực hiện truy vấn phân trang `findAll`. Test chỉ đọc; không ghi dữ liệu hoặc đổi schema.

## 5. Kết quả theo bốn yêu cầu

1. **JSP/JSTL:** cấu hình Spring Boot 4.1.2 hoạt động; các trang được render và hiển thị
   tiếng Việt với UTF-8.
2. **Tìm kiếm và phân trang:** Category hỗ trợ tìm kiếm, User hỗ trợ tìm kiếm/lọc;
   cả hai danh sách dùng phân trang.
3. **Đăng ký, đăng nhập, đăng xuất:** dùng Spring Security, BCrypt, kiểm tra trạng thái
   tài khoản, phân quyền theo role và bảo vệ CSRF.
4. **CRUD Category/User:** Admin quản lý được dữ liệu; User không được truy cập khu vực
   Admin. Form có validation và bảo vệ Admin hoạt động cuối cùng.

## 6. Kết luận

Bốn yêu cầu chính đã hoàn thành. `clean test package` và kiểm thử JDBC riêng với SQL Server
đều thành công.
