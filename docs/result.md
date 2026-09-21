# NHẬT KÝ KẾT QUẢ THỰC HIỆN DỰ ÁN (PROJECT LOG) — SnapLink

> Tài liệu ghi chép chi tiết kết quả thực hiện công việc theo từng ngày, đối chiếu với kế hoạch tại [plan.md](./plan.md).

---

## TUẦN 1 — Nền móng: Setup, DB, Auth, CRUD cơ bản

### 📅 Ngày 1: Setup Project, Kiến trúc Layered & Môi trường
- **Thời gian hoàn thành:** 19/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Khởi tạo & Hoàn thiện Dependencies (`pom.xml`):**
     - Đầy đủ các Starter: Web MVC, Data JPA, Data Redis, Security, Validation, Lombok, DevTools.
     - Tích hợp PostgreSQL Driver và Flyway Migration (`flyway-core`, `flyway-database-postgresql`).
  2. **Cấu hình môi trường & Database:**
     - Thiết lập file `.env` chứa thông tin kết nối Supabase PostgreSQL, Redis, cấu hình Server & JWT.
     - Cập nhật [application.yml](../src/main/resources/application.yml) ánh xạ biến môi trường và thiết lập JPA/Hibernate.
     - Tạo [docker-compose.yml](../docker-compose.yml) (Postgres 16 + Redis 7) làm phương án dự phòng cho local dev offline.
  3. **Xây dựng cấu trúc Layered Architecture:**
     - `com.snaplink.entity`: Quản lý các JPA Entity.
     - `com.snaplink.repository`: Tầng giao tiếp dữ liệu Spring Data JPA.
     - `com.snaplink.service`: Tầng nghiệp vụ (business logic).
     - `com.snaplink.controller`: Tầng API REST Controllers.
     - `com.snaplink.dto`: Chứa Request/Response Objects.
     - `com.snaplink.config`: Cấu hình hệ thống (Security, Redis, etc.).
     - `com.snaplink.exception`: Xử lý lỗi toàn cục.
  4. **Triển khai Global Exception Handling:**
     - Tạo `ResourceNotFoundException.java`, `BadRequestException.java`.
     - Tạo `ErrorResponse.java` (chuẩn hoá format JSON trả về khi có lỗi).
     - Tạo `GlobalExceptionHandler.java` (@RestControllerAdvice) tự động bắt lỗi Validation, Not Found, Bad Request và Exception chung.

---

### 📅 Ngày 2: Database Schema, Flyway Migration & JPA Entities
- **Thời gian hoàn thành:** 19/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Thiết kế Migration Script:**
     - Tạo file Flyway [V1__initial_schema.sql](../src/main/resources/db/migration/V1__initial_schema.sql) khởi tạo 3 bảng chính:
       - `users`: Quản lý tài khoản (`id`, `email`, `password_hash`, `created_at`).
       - `urls`: Quản lý link rút gọn (`id`, `short_code`, `original_url`, `custom_alias`, `user_id`, `expires_at`, `created_at`, `is_active`).
       - `click_events`: Lưu sự kiện click phục vụ analytics (`id`, `url_id`, `clicked_at`, `ip_address`, `country`, `device_type`, `browser`, `referrer`).
     - Thiết lập các khoá ngoại (`ON DELETE SET NULL`, `ON DELETE CASCADE`) và tạo 4 Index tối ưu hiệu năng: `idx_urls_short_code`, `idx_urls_user_id`, `idx_click_events_url_id`, `idx_click_events_clicked_at`.
  2. **Xây dựng JPA Entities (áp dụng Lombok `@FieldDefaults`):**
     - [User.java](../src/main/java/com/snaplink/entity/User.java): Mapping bảng `users`, quan hệ 1-N với `Url`.
     - [Url.java](../src/main/java/com/snaplink/entity/Url.java): Mapping bảng `urls`, quan hệ N-1 với `User` và 1-N với `ClickEvent`.
     - [ClickEvent.java](../src/main/java/com/snaplink/entity/ClickEvent.java): Mapping bảng `click_events`, quan hệ N-1 với `Url`.
  3. **Xây dựng Spring Data JPA Repositories:**
     - [UserRepository.java](../src/main/java/com/snaplink/repository/UserRepository.java): Các hàm `findByEmail`, `existsByEmail`.
     - [UrlRepository.java](../src/main/java/com/snaplink/repository/UrlRepository.java): Tìm kiếm theo `shortCode`, `customAlias`, lọc danh sách theo `userId`.
     - [ClickEventRepository.java](../src/main/java/com/snaplink/repository/ClickEventRepository.java): Thống kê `countByUrlId`, `findByUrlIdOrderByClickedAtDesc`.
  4. **Kiểm thử & Xác minh kết nối Supabase Cloud:**
     - Chạy `./mvnw test` thành công: Kết nối thông suốt tới Supabase qua HikariCP, JPA EntityManagerFactory khởi tạo thành công 100% với schema PostgreSQL.
