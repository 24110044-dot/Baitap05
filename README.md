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

