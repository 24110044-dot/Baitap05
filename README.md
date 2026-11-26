# Category Management

Ứng dụng quản lý danh mục và người dùng, xây dựng bằng Spring Boot MVC, JSP/JSTL,
Spring Security, Spring Data JPA và Microsoft SQL Server.

## Công nghệ và yêu cầu môi trường

- Java 17
- Spring Boot 4.1.2
- Maven Wrapper (`mvnw.cmd`)
- JSP/JSTL; ứng dụng được đóng gói dạng WAR
- Spring Security và BCrypt
- Spring Data JPA / Hibernate
- Microsoft SQL Server
- Visual Studio Code (khuyến nghị, có cấu hình chạy trong `.vscode/launch.json`)

## Cấu trúc dự án

```text
bt05/
├── .vscode/launch.json
├── pom.xml
├── mvnw
├── mvnw.cmd
├── HELP.md
└── src/
    ├── main/
    │   ├── java/com/example/bt05/
    │   │   ├── config/       # Security, khởi tạo Admin, phục vụ ảnh upload
    │   │   ├── controller/   # Auth, Dashboard, Category, User
    │   │   ├── dto/          # Form và validation
    │   │   ├── entity/       # Category, User
    │   │   ├── repository/   # Spring Data JPA repositories
    │   │   └── service/      # Nghiệp vụ tài khoản, Category, lưu ảnh
    │   ├── resources/
    │   │   └── application.properties
    │   └── webapp/
    │       ├── WEB-INF/views/
    │       │   ├── admin/categories/
    │       │   ├── admin/users/
    │       │   ├── auth/
    │       │   ├── layout/
    │       │   └── user/
    │       ├── css/app.css
    │       └── js/app.js
    └── test/
        ├── java/com/example/bt05/
        └── resources/
            ├── application-test.properties
            └── schema.sql
```

Main class hiện tại là `com.example.bt05.Bt05Application`.

## Cấu hình database

Ứng dụng sử dụng database đã có `CategoryManagementDB` với hai bảng `Categories`
và `Users`. Không tạo lại database hoặc thay đổi schema. Hibernate đặt
`spring.jpa.hibernate.ddl-auto=validate`, chỉ kiểm tra mapping khi khởi động.

Mặc định, cấu hình tại `src/main/resources/application.properties` kết nối tới
`MSI\\MSSQLSERVER01` bằng Windows Integrated Authentication. Cần bảo đảm SQL Server
đang chạy, database và các bảng hiện hữu đúng schema.

Nếu dùng SQL Authentication, cấu hình các biến môi trường:

- `DB_URL`: JDBC URL của SQL Server
- `DB_USERNAME`: tên đăng nhập SQL Server
- `DB_PASSWORD`: mật khẩu SQL Server

Windows Authentication cần native DLL `mssql-jdbc_auth` đúng phiên bản và kiến trúc
JDK. Hướng dẫn chi tiết về DLL và chạy kiểm tra với SQL Server thật nằm trong
[HELP.md](./HELP.md).

### Tạo Admin đầu tiên (tùy chọn)

Nếu cần ứng dụng khởi tạo Admin khi chưa có Admin hoạt động, cấu hình các biến môi trường:

- `BOOTSTRAP_ADMIN_ENABLED=true`
- `BOOTSTRAP_ADMIN_USERNAME`
- `BOOTSTRAP_ADMIN_EMAIL`
- `BOOTSTRAP_ADMIN_PASSWORD`

Mật khẩu được lưu dưới dạng BCrypt. Chỉ bật bootstrap khi cần khởi tạo và không commit
thông tin đăng nhập vào source control. Đăng ký công khai luôn tạo tài khoản `USER`.

### Ảnh Category

Form Category cho phép nhập URL HTTP/HTTPS hoặc tải ảnh PNG, JPEG, GIF từ máy (tối đa
5 MB, 12 megapixel). Ảnh tải lên được lưu trong `uploads/categories` theo mặc định;
có thể đổi thư mục bằng `APP_UPLOAD_DIRECTORY`. Cột `Categories.images` lưu URL hoặc
đường dẫn ảnh đã tải lên. Thư mục upload cần có quyền ghi và nên được sao lưu cùng database.

## Chức năng đã triển khai

### Đăng ký, đăng nhập, đăng xuất

- Đăng ký tài khoản với Username, Password, xác nhận mật khẩu, Email, FullName và Phone.
- Kiểm tra dữ liệu, Username/Email trùng và mật khẩu xác nhận.
- Mật khẩu được mã hóa BCrypt; đăng ký công khai không thể chọn quyền Admin.
- Đăng nhập kiểm tra tài khoản và trạng thái hoạt động; tài khoản bị khóa không đăng nhập được.
- Đăng nhập chuyển hướng Admin/User theo role.
- Đăng xuất bằng POST, xóa Authentication và invalidate session; form giữ CSRF token.
- Sau khi đăng nhập, User được chuyển đến trang chủ danh mục.

### Quản lý Category (chỉ Admin)

- Thêm, xem, sửa và xóa Category.
- Tìm kiếm theo tên không phân biệt hoa thường; phân trang bằng `Pageable`.
- Kiểm tra tên bắt buộc, tối đa 50 ký tự và không trùng.
- Giữ nguyên `CreatedAt` khi cập nhật.
- Ảnh hiển thị dưới dạng icon; hỗ trợ URL hoặc tải ảnh từ máy.
- Trạng thái Category bật (Active) sẽ hiển thị trên trang chủ User; Category tắt (Inactive)
  vẫn được lưu trong hệ thống nhưng không xuất hiện với User.

### Quản lý User (chỉ Admin)

- Thêm, xem, sửa và xóa User.
- Tìm kiếm theo từ khóa, lọc theo role và status; phân trang bằng `Pageable`.
- Kiểm tra Username/Email duy nhất.
- Mật khẩu được BCrypt hóa; không hiển thị mật khẩu hoặc hash.
- Khi sửa, để trống mật khẩu mới thì giữ mật khẩu hiện tại.
- Không cho xóa hoặc vô hiệu hóa Admin hoạt động cuối cùng.

## Các URL chính

| Method | URL | Chức năng |
|---|---|---|
| GET | `/register` | Hiển thị form đăng ký |
| POST | `/register` | Tạo tài khoản USER |
| GET | `/login` | Hiển thị form đăng nhập |
| POST | `/login` | Xác thực đăng nhập qua Spring Security |
| POST | `/logout` | Đăng xuất |
| GET | `/admin/dashboard` | Dashboard Admin |
| GET | `/user/dashboard` | Trang chủ User, hiển thị Category đang Active |
| GET | `/admin/categories` | Danh sách, tìm kiếm, phân trang Category |
| GET | `/admin/categories/create` | Form thêm Category |
| POST | `/admin/categories/store` | Lưu Category |
| GET | `/admin/categories/edit/{id}` | Form sửa Category |
| POST | `/admin/categories/update/{id}` | Cập nhật Category |
| POST | `/admin/categories/delete/{id}` | Xóa Category |
| GET | `/admin/users` | Danh sách, tìm kiếm, lọc, phân trang User |
| GET | `/admin/users/create` | Form thêm User |
| POST | `/admin/users/store` | Lưu User |
| GET | `/admin/users/edit/{id}` | Form sửa User |
| POST | `/admin/users/update/{id}` | Cập nhật User |
| POST | `/admin/users/delete/{id}` | Xóa User |

Danh sách hỗ trợ query parameters `keyword`, `page`, `size`; riêng User có thêm `role`
và `status`. Kích thước trang được giới hạn trong khoảng 5–50 bản ghi.

## Chạy ứng dụng

1. Cấu hình kết nối SQL Server và Windows Authentication (nếu sử dụng).
2. Từ PowerShell tại thư mục dự án, chạy:

   ```powershell
   .\mvnw.cmd clean test package
   ```

3. Có thể chạy cấu hình Java trong VS Code hoặc khởi động WAR đã đóng gói:

   ```powershell
   java -jar target\bt05.war
   ```

Mặc định ứng dụng chạy tại `http://localhost:8080`. Cần kết nối database thành công
trước khi sử dụng các chức năng.

## Kiểm thử

Kiểm thử ứng dụng dùng database H2 riêng trong `src/test/resources`; chúng xác minh
nghiệp vụ, JSP/JSTL, xác thực, CRUD, upload và phục vụ ảnh. Kiểm thử kết nối SQL Server
thật bị bỏ qua mặc định; xem [HELP.md](./HELP.md) để bật riêng kiểm thử tích hợp chỉ đọc.

## Báo cáo kiểm tra dự án

**Ngày kiểm tra:** 26/11/2026

**Phạm vi:** Đối chiếu cấu hình, Controller, Service, Repository, Entity, JSP/JSTL,
Spring Security và test hiện có. Không chỉnh sửa schema hoặc dữ liệu database trong quá trình audit.

### 1. Kết quả theo yêu cầu

| Yêu cầu | Đánh giá | Bằng chứng |
|---|---|---|
| JSP/JSTL trên Spring Boot 4.1.2 | **Đạt** | `pom.xml` khai báo Spring Boot 4.1.2, WAR, Tomcat Jasper, JSTL API và implementation. `application.properties` cấu hình view prefix/suffix và UTF-8. HTTP tests khởi động Tomcat, render JSP/JSTL và kiểm tra nội dung/charset. |
| Tìm kiếm, lọc và phân trang | **Đạt theo code và test H2** | Category tìm theo tên, không phân biệt hoa thường. User tìm theo username/email/full name, lọc role/status. Dùng Spring Data `Page`/`Pageable`; page size được giới hạn từ 5 đến 50. |
| Đăng ký, Login, Logout bằng Spring Security | **Đạt theo code và HTTP tests** | Public registration tạo role USER, kiểm tra dữ liệu và BCrypt password. Login chặn tài khoản inactive, điều hướng theo role. Logout POST invalidate session và xóa Authentication. CSRF đang bật theo cấu hình mặc định; các form POST có token. |
| CRUD Category/User chỉ dành cho Admin | **Đạt theo code và HTTP tests** | Toàn bộ `/admin/**` yêu cầu `ROLE_ADMIN`. Có danh sách, tạo, sửa, xóa; kiểm tra validation/unique; không cho vô hiệu hóa hoặc xóa Admin hoạt động cuối cùng. HTTP test xác nhận USER nhận 403 khi truy cập `/admin/categories`. |

### 2. Controller, URL và HTTP method

| Method | URL | Quyền / chức năng |
|---|---|---|
| GET | `/` | Chuyển khách sang login; chuyển người dùng đã đăng nhập theo role |
| GET | `/login` | Form đăng nhập |
| POST | `/login` | Spring Security xác thực đăng nhập |
| GET | `/register` | Form đăng ký |
| POST | `/register` | Tạo tài khoản USER |
| POST | `/logout` | Đăng xuất, yêu cầu CSRF token |
| GET | `/admin/dashboard` | Dashboard Admin |
| GET | `/user/dashboard` | Trang chủ User, chỉ liệt kê Category Active |
| GET | `/admin/categories` | Danh sách/tìm kiếm/phân trang Category |
| GET | `/admin/categories/create` | Form tạo Category |
| POST | `/admin/categories/store` | Tạo Category |
| GET | `/admin/categories/edit/{id}` | Form sửa Category |
| POST | `/admin/categories/update/{id}` | Cập nhật Category |
| POST | `/admin/categories/delete/{id}` | Xóa Category |
| GET | `/admin/users` | Danh sách/tìm kiếm/lọc/phân trang User |
| GET | `/admin/users/create` | Form tạo User |
| POST | `/admin/users/store` | Tạo User |
| GET | `/admin/users/edit/{id}` | Form sửa User |
| POST | `/admin/users/update/{id}` | Cập nhật User |
| POST | `/admin/users/delete/{id}` | Xóa User |

Category list nhận `keyword`, `page`, `size`. User list nhận thêm `role`, `status`.
Các link phân trang giữ điều kiện tìm kiếm/lọc hiện tại. Các thao tác xóa dùng POST và
có xác nhận phía trình duyệt.

### 3. Bảo mật và dữ liệu

- `SecurityConfig` dùng BCryptPasswordEncoder, custom UserDetailsService, role-based access,
  session fixation protection, session cookie HttpOnly/SameSite và logout invalidate session.
- CSRF không bị tắt; các form đăng ký, login, logout và CRUD gửi `_csrf`.
- Test xác minh đăng ký lưu hash BCrypt thay vì plaintext, tài khoản inactive không login,
  đăng ký không cấp ADMIN và USER không truy cập Category Admin.
- Category/User Entity ánh xạ các cột hiện có. `spring.jpa.hibernate.ddl-auto=validate`;
  audit không chạy migration, không tạo/xóa bảng và không thay đổi schema.

### 4. Kết quả kiểm thử

Lệnh bắt buộc:

```powershell
.\mvnw.cmd clean test package
```

Kết quả: **BUILD SUCCESS**. Có 11 test trong lượt chạy mặc định: 10 pass, 0 fail,
1 skipped (`SqlServerJdbcIntegrationTest`, do test này yêu cầu bật riêng).
H2 test cover service tìm kiếm/phân trang, BCrypt, bảo vệ Admin cuối cùng, HTTP login/logout,
403 cho USER, CRUD, render JSP/JSTL và upload/serve ảnh Category. Đây là kết quả H2,
không tự nó chứng minh kết nối SQL Server.

Đã chạy riêng kiểm thử JDBC tích hợp bằng Windows Authentication:

```powershell
$authDir = Join-Path $env:USERPROFILE '.m2\repository\com\microsoft\sqlserver\mssql-jdbc_auth\13.4.0.x64'
$env:PATH = "$authDir;$env:PATH"
.\mvnw.cmd '-Dsqlserver.integration=true' '-Dtest=SqlServerJdbcIntegrationTest' test
```

Kết quả: **BUILD SUCCESS, 1 test pass, 0 fail, 0 skipped**. Kiểm thử kết nối trực tiếp
đến `CategoryManagementDB`, chạy `SELECT 1`, đọc số dòng hai bảng hiện có bằng JDBC và
đối chiếu kết quả với JPA. Hibernate xác nhận SQL Server 16.0 và schema `dbo`.
Đây là phép xác minh kết nối thật trong môi trường tại thời điểm kiểm tra; test chỉ đọc,
không ghi dữ liệu hoặc thay đổi schema.

### 5. Yêu cầu chưa hoàn thành và giới hạn kiểm tra

Không phát hiện yêu cầu chức năng chính nào trong bốn nhóm bị thiếu khi đối chiếu code và test.
Các điểm sau chưa được khẳng định bởi bộ test hiện tại:

- Chưa có test riêng xác nhận server trả 403 với CSRF token thiếu/sai; test HTTP hiện gửi token
  hợp lệ cho các POST.
- Phân trang/tìm kiếm được kiểm chứng bằng H2 service test; chưa có kiểm thử truy vấn phân trang
  và tìm kiếm trên SQL Server thật.
- Chưa kiểm thử trực quan mọi trang trên các kích thước desktop/tablet/mobile hoặc trong một
  deployment ngoài Tomcat nhúng.
- URL ảnh bên ngoài phụ thuộc máy chủ ảnh và quyền truy cập của trình duyệt; JDBC integration
  không xác minh các URL ngoài này.

Đây là giới hạn xác minh thủ công/kiểm thử, không phải kết luận rằng các chức năng trên đang lỗi.

### 6. Chức năng nên kiểm tra thủ công trước khi báo cáo/demo

1. Trên SQL Server thật, đăng nhập bằng Admin và User hiện có; xác nhận route dashboard theo role.
2. Thử User truy cập trực tiếp từng `/admin/categories/**` và `/admin/users/**`; xác nhận bị từ chối.
3. Với dữ liệu SQL Server, thử tìm kiếm, lọc, đổi page size và chuyển trang ở cả hai danh sách.
4. Thử thêm/sửa/xóa Category/User, gồm tên/email trùng, form không hợp lệ và bảo vệ Admin cuối cùng.
5. Gửi POST thiếu CSRF token để xác nhận request bị từ chối; không dùng thao tác này để thay đổi dữ liệu.
6. Xác nhận upload/URL ảnh và thư mục lưu `APP_UPLOAD_DIRECTORY` có quyền ghi/backup trong môi trường chạy.
7. Kiểm tra hiển thị tiếng Việt và bố cục trên trình duyệt/thiết bị mục tiêu.

**Đánh giá cuối:** Bốn yêu cầu chính được triển khai theo code và test; build package thành công.
JSP/JSTL đã được render qua HTTP test. Kết nối SQL Server thật cũng đã được kiểm chứng riêng bằng
JDBC read-only integration test trong lượt audit này. Không có thay đổi schema database.

