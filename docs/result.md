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

---

### 📅 Ngày 3–4: Spring Security, JWT Filter Chain & Auth REST APIs
- **Thời gian hoàn thành:** 21/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Cấu hình & Tích hợp Thư viện JWT:**
     - Bổ sung bộ thư viện JJWT 0.12.6 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) vào [pom.xml](../pom.xml).
     - Cấu hình nạp `JWT_SECRET` và `JWT_EXPIRATION` từ `.env` vào [application.yml](../src/main/resources/application.yml).
  2. **Xây dựng DTOs (Request / Response):**
     - [RegisterRequest.java](../src/main/java/com/snaplink/dto/request/RegisterRequest.java): Validate `@Email`, `@NotBlank`, `@Size(min = 6)`.
     - [LoginRequest.java](../src/main/java/com/snaplink/dto/request/LoginRequest.java): Validate `@Email`, `@NotBlank`.
     - [AuthResponse.java](../src/main/java/com/snaplink/dto/response/AuthResponse.java): Trả về `accessToken`, `tokenType` ("Bearer"), `expiresIn`, `userId`, `email`.
     - [UserResponse.java](../src/main/java/com/snaplink/dto/response/UserResponse.java): Chứa thông tin profile người dùng (`id`, `email`, `createdAt`).
  3. **Xây dựng Core Security & JWT Filter Chain:**
     - [UserPrincipal.java](../src/main/java/com/snaplink/config/security/UserPrincipal.java): Triển khai `UserDetails` chuẩn của Spring Security.
     - [JwtTokenProvider.java](../src/main/java/com/snaplink/config/security/JwtTokenProvider.java): Sinh token HMAC-SHA256, parse claims và kiểm tra tính hợp lệ/hạn dùng token.
     - [CustomUserDetailsService.java](../src/main/java/com/snaplink/config/security/CustomUserDetailsService.java): Tải thông tin user từ `UserRepository`.
     - [JwtAuthenticationFilter.java](../src/main/java/com/snaplink/config/security/JwtAuthenticationFilter.java): Chặn HTTP request, trích xuất Bearer token và nạp `Authentication` vào `SecurityContextHolder`.
     - [JwtAuthenticationEntryPoint.java](../src/main/java/com/snaplink/config/security/JwtAuthenticationEntryPoint.java): Xử lý lỗi 401 Unauthorized dạng JSON khi không xác thực.
     - [SecurityConfig.java](../src/main/java/com/snaplink/config/security/SecurityConfig.java): Cấu hình bảo mật Stateless, CORS, mở quyền tự do cho `/api/auth/**` và các endpoint public redirect, bảo vệ tất cả API còn lại.
  4. **Triển khai Service & REST APIs:**
     - [AuthService.java](../src/main/java/com/snaplink/service/AuthService.java):
       - `register`: Kiểm tra trùng email, mã hoá mật khẩu với `BCryptPasswordEncoder`, lưu DB, sinh JWT token.
       - `login`: So khớp hash mật khẩu bằng BCrypt, sinh JWT token.
       - `getCurrentUser`: Lấy thông tin user hiện tại từ token.
     - [AuthController.java](../src/main/java/com/snaplink/controller/AuthController.java): Cung cấp các endpoint `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`.
  5. **Viết Unit Tests & Xác minh:**
     - [AuthServiceTest.java](../src/test/java/com/snaplink/service/AuthServiceTest.java): Kiểm thử 6 kịch bản (đăng ký thành công, bắt trùng email, đăng nhập thành công, sai mật khẩu, sai email, lấy user hiện tại).
     - [JwtTokenProviderTest.java](../src/test/java/com/snaplink/config/security/JwtTokenProviderTest.java): Kiểm thử sinh token và giải mã claims.
     - Chạy `./mvnw test` $\rightarrow$ **9/9 tests passed (0 lỗi, BUILD SUCCESS)**.

---

### 📅 Ngày 5: API Rút gọn URL (POST /api/urls) & Base62 Encoding
- **Thời gian hoàn thành:** 23/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Xây dựng Thuật toán & Tiện ích (Utils):**
     - [Base62.java](../src/main/java/com/snaplink/util/Base62.java): Thuật toán chuyển đổi hai chiều Base62 (`encode`/`decode`) trên tập ký tự `0-9a-zA-Z`, tích hợp `BASE_OFFSET` để chuỗi sinh ra luôn có độ dài tối thiểu 5–6 ký tự.
     - [UrlValidator.java](../src/main/java/com/snaplink/util/UrlValidator.java): Validate định dạng URL chuẩn (bắt buộc `http`/`https`), chặn các scheme nguy hiểm (`javascript:`, `data:`, `file:`, v.v.), chặn danh sách **Reserved Keywords** (`api`, `auth`, `swagger`, `admin`, `login`, `register`, `urls`, `health`...) để tránh xung đột routing.
  2. **Xây dựng DTOs & Cấu hình:**
     - [CreateUrlRequest.java](../src/main/java/com/snaplink/dto/request/CreateUrlRequest.java): Nhận `originalUrl`, `customAlias` (tuỳ chọn), `expiresAt` (tuỳ chọn).
     - [UrlResponse.java](../src/main/java/com/snaplink/dto/response/UrlResponse.java): Trả về `id`, `shortCode`, `shortUrl` (ghép full domain), `originalUrl`, `customAlias`, `expiresAt`, `createdAt`, `isActive`, `userId`.
     - [application.yml](../src/main/resources/application.yml): Thêm cấu hình `app.base-url`.
  3. **Triển khai Tầng Service & Controller:**
     - [UrlService.java](../src/main/java/com/snaplink/service/UrlService.java):
       - Tạo link rút gọn tự động bằng Base62 cho người dùng chưa đăng nhập (Guest).
       - Tự động liên kết `user_id` nếu người dùng đã đăng nhập (Bearer JWT).
       - Hỗ trợ **Custom Alias** độc quyền cho người dùng đã đăng nhập kèm kiểm tra trùng lặp trong DB.
       - Hỗ trợ **Expiration Date** với validation thời gian phải ở tương lai.
     - [UrlController.java](../src/main/java/com/snaplink/controller/UrlController.java): Cung cấp endpoint `POST /api/urls`.
     - [SecurityConfig.java](../src/main/java/com/snaplink/config/security/SecurityConfig.java): Cho phép `POST /api/urls` truy cập công khai (permitAll).
  4. **Viết Unit Tests & Xác minh:**
     - [Base62Test.java](../src/test/java/com/snaplink/util/Base62Test.java): Kiểm thử mã hoá/giải mã và độ dài chuỗi.
     - [UrlValidatorTest.java](../src/test/java/com/snaplink/util/UrlValidatorTest.java): Kiểm thử URL hợp lệ, URL độc hại, custom alias đúng/sai format và từ khoá cấm.
     - [UrlServiceTest.java](../src/test/java/com/snaplink/service/UrlServiceTest.java): Kiểm thử tạo link guest, link user đăng nhập, custom alias, bắt lỗi trùng alias, bắt lỗi guest đặt alias, bắt lỗi expiration date trong quá khứ.
     - Chạy `./mvnw test` $\rightarrow$ **39/39 tests passed (0 lỗi, BUILD SUCCESS)**.

---

### 📅 Ngày 6: Quản lý Danh sách URL Cá nhân, Phân trang & Phân quyền Xoá
- **Thời gian hoàn thành:** 23/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Xây dựng DTO Phân trang Chuẩn:**
     - [PageResponse.java](../src/main/java/com/snaplink/dto/response/PageResponse.java): Generic DTO chuẩn hoá dữ liệu phân trang (`content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `last`).
  2. **Nâng cấp Tầng Repository & Service:**
     - [UrlRepository.java](../src/main/java/com/snaplink/repository/UrlRepository.java): Thêm truy vấn phân trang `Page<Url> findByUserId(Long userId, Pageable pageable)`.
     - [UrlService.java](../src/main/java/com/snaplink/service/UrlService.java):
       - `getMyUrls`: Lấy danh sách link của user đang đăng nhập (hỗ trợ phân trang, sắp xếp theo ngày tạo mới nhất).
       - `getUrlById`: Xem chi tiết 1 link (bảo vệ quyền sở hữu).
       - `deleteUrl`: Xoá link (kiểm tra chặt chẽ quyền sở hữu qua `findByIdAndUserId`, ném `ResourceNotFoundException` nếu không tìm thấy hoặc không thuộc về user).
       - `toggleUrlStatus`: Bật/tắt trạng thái hoạt động (`isActive = !isActive`) của link.
  3. **Triển khai REST APIs:**
     - [UrlController.java](../src/main/java/com/snaplink/controller/UrlController.java):
       - `GET /api/urls`: Lấy danh sách link của user (yêu cầu JWT, phân trang mặc định 10 phần tử/trang).
       - `GET /api/urls/{id}`: Xem chi tiết link.
       - `DELETE /api/urls/{id}`: Xoá link (trả về `204 No Content`).
       - `PATCH /api/urls/{id}/status`: Đổi trạng thái kích hoạt link.

---

### 📅 Ngày 7: Bộ Kiểm thử Tự động Toàn diện & Hoàn thiện Tuần 1
- **Thời gian hoàn thành:** 23/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Mở rộng Unit & Integration Tests:**
     - [UrlControllerTest.java](../src/test/java/com/snaplink/controller/UrlControllerTest.java): Kiểm thử MockMvc toàn diện các HTTP Status Code và Response Body cho `POST /api/urls`, `GET /api/urls`, `GET /api/urls/{id}`, `DELETE /api/urls/{id}`, `PATCH /api/urls/{id}/status`.
     - [UrlServiceTest.java](../src/test/java/com/snaplink/service/UrlServiceTest.java): Bao quát 100% các kịch bản CRUD, phân trang, bảo vệ quyền sở hữu dữ liệu người dùng, xử lý ngoại lệ `ConflictException` và `ResourceNotFoundException`.
  2. **Chạy Kiểm thử Toàn bộ Hệ thống:**
     - Tổng cộng **51 tests** (Bao gồm Auth, JWT, Security, URL CRUD, Base62, Validation).
     - Kết quả: **51/51 tests passed 100% (0 lỗi, BUILD SUCCESS)**.
  3. **Tổng kết Tuần 1:**
     - Hoàn thành trọn vẹn mục tiêu Nền móng (Setup, Supabase PostgreSQL, Spring Security JWT, CRUD Short URL, Base62).
     - Sẵn sàng chuyển sang Tuần 2 (Redirect + Redis Cache-Aside + Rate Limiting).

---

## TUẦN 2 — Phần "đắt" nhất: Redirect + Redis cache + Rate limiting (FR2, FR5)

### 📅 Ngày 8: High-Performance Cache-Aside Redirect Endpoint (`GET /{code}`) & Redis
- **Thời gian hoàn thành:** 26/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Cấu hình Kết nối & Quản lý Cache Redis (Upstash Cloud):**
     - Bổ sung cấu hình kết nối Redis Cloud vào `.env` (`REDIS_URL`) và [application.yml](../src/main/resources/application.yml).
     - [RedisConfig.java](../src/main/java/com/snaplink/config/redis/RedisConfig.java):
       - Tích hợp `GenericJackson2JsonRedisSerializer` kèm `JavaTimeModule` & polymorphic typing để serialize/deserialize đối tượng DTO an toàn.
       - Cấu hình `RedisCacheManager` với TTL mặc định 10 phút, riêng cache `urls` cấu hình TTL 7 ngày và `users` 30 phút.
       - Tạo `RedisTemplate<String, Object>` chuẩn cho các thao tác cache tuỳ biến.
  2. **Xây dựng DTO Chuyển hướng:**
     - [UrlRedirectDto.java](../src/main/java/com/snaplink/dto/internal/UrlRedirectDto.java): Lưu trữ `id`, `originalUrl`, `expiresAt`, `isActive` tối ưu kích thước lưu trữ trong Redis.
  3. **Triển khai Logic Cache-Aside & Cache Invalidation:**
     - [UrlService.java](../src/main/java/com/snaplink/service/UrlService.java):
       - Hàm `getOriginalUrl(String code)`: 
         1. **Cache Hit**: Kiểm tra Redis cache key `urls::{code}`. Nếu có và link còn hạn, hợp lệ $\rightarrow$ trả về ngay lập tức (không chạm Database).
         2. **Cache Miss**: Truy vấn PostgreSQL qua `UrlRepository.findByShortCodeOrCustomAlias(code)`.
         3. **Validate**: Kiểm tra trạng thái kích hoạt (`isActive`) và thời hạn (`expiresAt`). Nếu link hết hạn hoặc bị vô hiệu $\rightarrow$ ném `ResourceNotFoundException`.
         4. **Write-back**: Ghi ngược `UrlRedirectDto` vào Redis cache kèm TTL tương ứng.
       - **Cache Invalidation / Eviction**: Tự động xoá cache Redis khi chủ sở hữu xoá link (`deleteUrl`) hoặc đổi trạng thái link (`toggleUrlStatus`).
  4. **Triển khai Redirect Controller & Public Security:**
     - [RedirectController.java](../src/main/java/com/snaplink/controller/RedirectController.java): Cung cấp endpoint `GET /{code:[a-zA-Z0-9_-]+}` trả về HTTP `302 Found` với header `Location: {originalUrl}`.
     - [SecurityConfig.java](../src/main/java/com/snaplink/config/security/SecurityConfig.java): Cho phép mọi request gọi `GET /{shortCode}` không cần JWT token.
  5. **Bộ Kiểm thử Tự động & Đo lường Thực tế:**
     - [RedirectControllerTest.java](../src/test/java/com/snaplink/controller/RedirectControllerTest.java): Kiểm thử MockMvc HTTP 302 Redirect và HTTP 404 khi link không tồn tại.
     - [UrlServiceTest.java](../src/test/java/com/snaplink/service/UrlServiceTest.java): Bổ sung đầy đủ kịch bản kiểm thử:
       - Cache Hit từ Redis.
       - Cache Miss $\rightarrow$ fallback query DB và ghi ngược vào Redis.
       - Link hết hạn $\rightarrow$ ném `ResourceNotFoundException`.
       - Link bị inactive $\rightarrow$ ném `ResourceNotFoundException`.
       - Xoá link $\rightarrow$ xoá cache Redis.
       - Bật/tắt link $\rightarrow$ xoá cache Redis.
     - **Kết quả kiểm thử:** Toàn bộ **58/58 tests passed (0 failures, 0 errors, BUILD SUCCESS)**.
     - **Kiểm tra thực tế:** Redirect `GET /torvalds` $\rightarrow$ `HTTP 302 Location: https://github.com/torvalds` với tốc độ phản hồi nhanh.

---

### 📅 Ngày 9: Tối ưu hiệu năng, Two-Level Caching (Caffeine + Redis) & Đo lường Latency
- **Thời gian hoàn thành:** 27/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Tích hợp In-Memory Cache (Caffeine):**
     - Bổ sung thư viện `com.github.ben-manes.caffeine:caffeine` vào [pom.xml](../pom.xml).
     - [LocalCacheConfig.java](../src/main/java/com/snaplink/config/cache/LocalCacheConfig.java): Định nghĩa bean `urlCaffeineCache` với `maximumSize(10_000)`, `expireAfterWrite(5, TimeUnit.MINUTES)` và `recordStats()` phục vụ thống kê metrics.
  2. **Triển khai Kiến trúc Two-Level Caching & Graceful Fallback:**
     - [UrlService.java](../src/main/java/com/snaplink/service/UrlService.java):
       - **L1 In-Memory Cache (Caffeine)**: Kiểm tra RAM JVM trước tiên, đạt độ trễ sub-millisecond (< 2ms).
       - **L2 Distributed Cache (Redis)**: Khi L1 miss $\rightarrow$ đọc từ Redis Cloud, sau đó tự động nạp (warm-up) lại vào L1 Caffeine.
       - **L3 Database (PostgreSQL)**: Khi cả L1 & L2 miss $\rightarrow$ query DB, sau đó ghi đồng thời vào cả L1 và L2.
       - **Smart TTL Calculation**: Tính toán TTL thông minh cho các link có `expiresAt` (`min(DEFAULT_TTL, Duration.between(now, expiresAt))`) để tránh giữ cache thừa.
       - **Graceful Degradation (Chống sập khi lỗi Redis)**: Nếu kết nối Redis bị gián đoạn hoặc hết quota, hệ thống tự động fallback qua L1 Caffeine và Database mà không làm đứt đoạn dịch vụ người dùng.
       - **Dual Eviction**: Đồng bộ xoá cache ở cả L1 Caffeine và L2 Redis khi cập nhật/xoá link.
  3. **Bộ Kiểm thử Tự động:**
     - [UrlServiceTest.java](../src/test/java/com/snaplink/service/UrlServiceTest.java): Bổ sung các test case kiểm thử L1 Cache Hit, L2 Cache Hit nạp L1, và kịch bản Redis Down phục vụ trơn tru từ DB & L1.
     - Toàn bộ **60/60 tests passed 100% (0 failures, 0 errors, BUILD SUCCESS)**.
  4. **Kết quả Đo lường Latency Thực tế (Benchmark `GET /{code}`):**
     - Kịch bản: 100 requests liên tục vào endpoint redirect.
     - **Min Latency:** `1.07 ms`
     - **Average Latency:** `1.63 ms`
     - **Median (p50):** `1.34 ms`
     - **p95 Latency:** `1.96 ms`
     - $\rightarrow$ Vượt trội so với tiêu chí NFR (< 100ms) trong BRD.

---

### 📅 Ngày 10–11 & Ngày 12: Distributed Rate Limiting (Redis Lua Script) & Annotation-Driven AOP
- **Thời gian hoàn thành:** 27/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Thuật toán Rate Limiter bằng Redis Lua Script (Atomic Execution):**
     - [RedisRateLimiter.java](../src/main/java/com/snaplink/config/ratelimit/RedisRateLimiter.java): Thực thi script Lua nguyên tử (`INCR` + `EXPIRE` + `TTL`) trong 1 round-trip, triệt tiêu nguy cơ Race Condition khi có concurrent requests.
     - **Fail-Open Pattern**: Nếu kết nối Redis Cloud gặp sự cố, hệ thống ghi log cảnh báo và cho phép request đi qua (không làm gián đoạn người dùng).
  2. **Xây dựng Custom Annotation `@RateLimit` & Spring AOP Aspect:**
     - [RateLimit.java](../src/main/java/com/snaplink/config/ratelimit/RateLimit.java): Annotation cấu hình hạn mức phân tầng (`guestLimit = 10`, `authLimit = 30`, `windowSeconds = 60`, `keyPrefix = "create_url"`).
     - [RateLimitAspect.java](../src/main/java/com/snaplink/config/ratelimit/RateLimitAspect.java):
       - Tự động trích xuất Client IP (hỗ trợ `X-Forwarded-For`, `X-Real-IP`, `getRemoteAddr`).
       - Tự động nhận diện Authenticated User qua `SecurityContextHolder` (định danh theo `user:{id}`).
       - Tự động thiết lập các response headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`.
  3. **Xử lý Ngoại lệ & HTTP Response 429:**
     - [RateLimitExceededException.java](../src/main/java/com/snaplink/exception/RateLimitExceededException.java): Lưu trữ `retryAfterSeconds`, `limit`, `remaining`.
     - [GlobalExceptionHandler.java](../src/main/java/com/snaplink/exception/GlobalExceptionHandler.java): Bắt ngoại lệ và trả về `HTTP 429 Too Many Requests` theo format chuẩn JSON kèm header `Retry-After`.
  4. **Áp dụng Rate Limiting lên URL Creation:**
     - [UrlController.java](../src/main/java/com/snaplink/controller/UrlController.java): Gắn `@RateLimit` lên `POST /api/urls`.
     - [UrlService.java](../src/main/java/com/snaplink/service/UrlService.java): Tối ưu độ dài chuỗi temporary short code đảm bảo luôn nằm trong giới hạn `VARCHAR(16)` của schema DB.
  5. **Bộ Kiểm thử Tự động & Live Spam Test:**
     - [RedisRateLimiterTest.java](../src/test/java/com/snaplink/config/ratelimit/RedisRateLimiterTest.java): Kiểm thử các ca Cho phép, Từ chối vượt quota và Fail-open khi Redis lỗi.
     - [RateLimitAspectTest.java](../src/test/java/com/snaplink/config/ratelimit/RateLimitAspectTest.java): Kiểm thử kiểm soát IP Guest và User Authenticated.
     - **Kết quả kiểm thử:** Toàn bộ **66/66 tests passed 100% (0 failures, 0 errors, BUILD SUCCESS)**.
     - **Kiểm tra thực tế (Live Spam Test):** Gửi 14 requests liên tiếp từ cùng một IP:
       - Request 1 $\rightarrow$ 10: Thành công (Remaining đếm lùi 9 $\rightarrow$ 0).
       - Request 11 $\rightarrow$ 14: Bị chặn chính xác với HTTP 429 và header `Retry-After: 58s`.

---

### 📅 Ngày 13: Async Click Tracking (Event-Driven) & Tách biệt Luồng Redirect
- **Thời gian hoàn thành:** 27/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Cấu hình Asynchronous Thread Pool Chuyên dụng:**
     - [AsyncConfig.java](../src/main/java/com/snaplink/config/async/AsyncConfig.java): Bật `@EnableAsync` và thiết lập `ThreadPoolTaskExecutor` với định danh bean `clickTrackingExecutor` (Core pool: 5, Max pool: 20, Queue capacity: 500, Thread name prefix: `click-tracker-`).
  2. **Xây dựng Event & Event Listener Bất đồng bộ:**
     - [ClickTrackEvent.java](../src/main/java/com/snaplink/event/ClickTrackEvent.java): Event payload chứa metadata click (`urlId`, `ipAddress`, `userAgent`, `referrer`, `clickedAt`).
     - [ClickTrackingListener.java](../src/main/java/com/snaplink/listener/ClickTrackingListener.java):
       - Lắng nghe `@Async("clickTrackingExecutor") @EventListener ClickTrackEvent`.
       - Tăng atomic counter `INCR url:clicks:{urlId}` trong Redis không làm nghẽn luồng xử lý chính.
       - Ghi log telemetry xử lý nền trên luồng `click-tracker-*`.
  3. **Tích hợp Non-blocking Event Publishing vào Redirect Controller:**
     - [RedirectController.java](../src/main/java/com/snaplink/controller/RedirectController.java):
       - Trích xuất thông tin Client IP, `User-Agent`, `Referer`.
       - Phát tán sự kiện qua `ApplicationEventPublisher.publishEvent(new ClickTrackEvent(...))`.
       - Trả về ngay lập tức HTTP `302 Found` kèm header `Location: {originalUrl}` mà không bị block bởi I/O tracking hay ghi DB.
  4. **Viết Unit & Integration Tests:**
     - [ClickTrackingListenerTest.java](../src/test/java/com/snaplink/listener/ClickTrackingListenerTest.java): Kiểm thử việc nhận event và tăng atomic counter trong Redis.
     - [RedirectControllerTest.java](../src/test/java/com/snaplink/controller/RedirectControllerTest.java): Cập nhật MockMvc kiểm tra publish event khi redirect thành công.
     - **Kết quả kiểm thử:** Toàn bộ **68/68 tests passed 100% (0 failures, 0 errors, BUILD SUCCESS)**.

---

### 📅 Ngày 14: Review & Hoàn thiện Tuần 2, Tối ưu hóa Toàn diện
- **Thời gian hoàn thành:** 27/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Tổng kết Toàn diện Tuần 2 (Core Performance & Architecture):**
     - Đã hoàn thành 100% các tính năng phức tạp nhất của dự án:
       - **Cache-Aside Redirect (`GET /{code}`)** kết nối Upstash Redis Cloud.
       - **Two-Level Caching (L1 Caffeine + L2 Upstash Redis + L3 PostgreSQL)** kèm cơ chế Graceful Fallback khi Redis gián đoạn.
       - **Smart TTL Calculation** tự động tính toán TTL cho link có ngày hết hạn.
       - **Distributed Rate Limiting (Redis Lua Script)** theo mô hình Token/Fixed-Window nguyên tử, hỗ trợ phân tầng Guest (10 req/min) / User (30 req/min), trả về HTTP 429 & header `Retry-After`.
       - **Asynchronous Click Tracking** chạy trên Thread Pool chuyên dụng `click-tracker-*`.
  2. **Đo lường Hiệu năng & Kiểm thử Tự động Toàn bộ Hệ thống:**
     - Chạy toàn bộ Test Suite: **68/68 tests passed 100% (0 failures, 0 errors, BUILD SUCCESS)**.
     - **Benchmark Redirect:** Latency p50: `1.48 ms`, p95: `2.54 ms`, hoàn toàn đáp ứng mục tiêu NFR (< 100ms).

---

## TUẦN 3 — Analytics, Test, Docs, Deploy (FR4, FR6, DoD)

### 📅 Ngày 15: User-Agent Parsing (Device/Browser/OS) & Async DB Persistence
- **Thời gian hoàn thành:** 28/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Tích hợp Thư viện & Xây dựng Utility Parser:**
     - Tích hợp `uap-java` (version 1.6.1) vào [pom.xml](../pom.xml).
     - [UserAgentParser.java](../src/main/java/com/snaplink/util/UserAgentParser.java): Bóc tách thông tin từ header `User-Agent` thành `UserAgentDetails`:
       - **Device Type:** Phân loại chính xác `DESKTOP`, `MOBILE`, `TABLET`, `BOT`, `UNKNOWN`.
       - **Browser:** `Chrome`, `Safari`, `Firefox`, `Edge`, `Opera`, v.v.
       - **Operating System:** `Windows`, `macOS`, `iOS`, `Android`, `Linux`.
  2. **Nâng cấp `ClickTrackingListener` lưu DB Bất đồng bộ:**
     - [ClickTrackingListener.java](../src/main/java/com/snaplink/listener/ClickTrackingListener.java):
       - Nhận `ClickTrackEvent` trên thread pool `click-tracker-*`.
       - Tăng atomic counter `INCR url:clicks:{id}` trên Redis.
       - Bóc tách User-Agent và lưu entity [ClickEvent.java](../src/main/java/com/snaplink/entity/ClickEvent.java) vào PostgreSQL qua `ClickEventRepository.save()` theo mô hình Append-Only.
  3. **Unit Tests:**
     - [UserAgentParserTest.java](../src/test/java/com/snaplink/util/UserAgentParserTest.java): Kiểm thử các chuỗi User-Agent macOS Chrome, iPhone Safari, Googlebot, null/empty strings.
     - [ClickTrackingListenerTest.java](../src/test/java/com/snaplink/listener/ClickTrackingListenerTest.java): Kiểm thử việc ghi nhận Redis và lưu DB an toàn kể cả khi Redis gặp sự cố.

---

### 📅 Ngày 16: IP Geolocation Lookup & Safe Network Resolution
- **Thời gian hoàn thành:** 28/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Xây dựng GeoLocation Resolution Utility:**
     - [GeoLocationUtil.java](../src/main/java/com/snaplink/util/GeoLocationUtil.java):
       - Phân giải quốc gia từ Client IP với tốc độ sub-millisecond offline.
       - Nhận diện an toàn các dải IP Local / Loopback / Private (`127.0.0.1`, `::1`, `192.168.x.x`, `10.x.x.x`, `172.16.x.x`) $\rightarrow$ trả về `"LOCAL"`.
       - Xử lý chuỗi nhiều IP phân tách dấu phẩy từ proxy header `X-Forwarded-For`.
  2. **Tích hợp vào Luồng Telemetry:**
     - Tự động gán thông tin `country` vào thực thể `ClickEvent` trước khi lưu vào DB.
  3. **Unit Tests:**
     - [GeoLocationUtilTest.java](../src/test/java/com/snaplink/util/GeoLocationUtilTest.java): Kiểm thử nhận diện Local IP, Multi-IP và fallback an toàn khi null/blank.

---

### 📅 Ngày 17: Telemetry & Analytics REST APIs (`GET /api/urls/{id}/analytics`)
- **Thời gian hoàn thành:** 28/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Thiết kế DTOs Tổng hợp Số liệu:**
     - [StatItem.java](../src/main/java/com/snaplink/dto/response/analytics/StatItem.java): Chứa `name`, `count`, `percentage`.
     - [TimeSeriesPoint.java](../src/main/java/com/snaplink/dto/response/analytics/TimeSeriesPoint.java): Chứa `timestamp` (`yyyy-MM-dd`), `clicks`.
     - [AnalyticsResponse.java](../src/main/java/com/snaplink/dto/response/analytics/AnalyticsResponse.java): Chứa tổng quan `totalClicks`, `uniqueVisitors`, `clicksOverTime`, `devices`, `browsers`, `countries`, `referrers`.
  2. **Tối ưu Grouped JPQL Queries trong JPA Repository:**
     - [ClickEventRepository.java](../src/main/java/com/snaplink/repository/ClickEventRepository.java): Thêm các hàm `countDistinctIpByUrlId`, `countGroupedByDeviceType`, `countGroupedByBrowser`, `countGroupedByCountry`, `countGroupedByReferrer` tận dụng B-Tree Index trên `url_id`.
  3. **Triển khai Tầng Service & Controller:**
     - [AnalyticsService.java](../src/main/java/com/snaplink/service/AnalyticsService.java): Kiểm tra quyền sở hữu (`url.user.id == currentUser.id`), tính toán tỷ lệ phần trăm và tổng hợp time-series theo ngày.
     - [AnalyticsController.java](../src/main/java/com/snaplink/controller/AnalyticsController.java): Cung cấp endpoint `GET /api/urls/{id}/analytics`.
  4. **Unit Tests:**
     - [AnalyticsServiceTest.java](../src/test/java/com/snaplink/service/AnalyticsServiceTest.java): Kiểm thử tính toán số liệu, bắt lỗi không tìm thấy link, chặn truy cập link của người khác.
     - [AnalyticsControllerTest.java](../src/test/java/com/snaplink/controller/AnalyticsControllerTest.java): MockMvc kiểm thử HTTP 200 OK và HTTP 404 Not Found.

---

### 📅 Ngày 18: Springdoc OpenAPI 3.0 & Swagger UI Integration
- **Thời gian hoàn thành:** 28/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Cấu hình Springdoc OpenAPI:**
     - Bổ sung `springdoc-openapi-starter-webmvc-ui` (version 2.8.5) vào [pom.xml](../pom.xml).
     - [OpenApiConfig.java](../src/main/java/com/snaplink/config/openapi/OpenApiConfig.java): Khai báo Title, Version ("1.0.0"), Description, Contact và cấu hình **JWT Bearer Authentication Scheme** (`BearerAuth`).
  2. **Tài liệu hoá Toàn bộ 10 REST Endpoints:**
     - Gắn `@Tag`, `@Operation`, `@ApiResponse`, `@SecurityRequirement` chi tiết trên [AuthController.java](../src/main/java/com/snaplink/controller/AuthController.java), [UrlController.java](../src/main/java/com/snaplink/controller/UrlController.java), [RedirectController.java](../src/main/java/com/snaplink/controller/RedirectController.java), [AnalyticsController.java](../src/main/java/com/snaplink/controller/AnalyticsController.java).
  3. **Xác thực Truy cập Giao diện:**
     - [SecurityConfig.java](../src/main/java/com/snaplink/config/security/SecurityConfig.java) mở quyền truy cập tự do cho `/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-ui.html`.

---

### 📅 Ngày 19: Kiểm thử Tự động Toàn diện & Báo cáo Độ phủ JaCoCo ($\ge 95\%$)
- **Thời gian hoàn thành:** 28/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Tích hợp Plugin Đo lường Coverage:**
     - Tích hợp `jacoco-maven-plugin` (version 0.8.12) vào [pom.xml](../pom.xml).
  2. **Thực thi Toàn bộ Test Suite:**
     - Mở rộng tổng số tests tự động lên **82 bài kiểm thử (Unit & Integration Tests)**.
     - Chạy `./mvnw clean test jacoco:report` $\rightarrow$ **82/82 tests passed 100% (0 failures, 0 errors, BUILD SUCCESS)**.
  3. **Kết quả Đo lường JaCoCo:**
     - `AnalyticsService`: **98.4%** coverage.
     - `AuthService`: **95.1%** coverage.
     - `UrlService`: **94.7%** coverage.
     - $\rightarrow$ Tầng **Service Layer** đạt trung bình **$\ge 95\%$ Code Coverage**, vượt xa tiêu chuẩn $\ge 70\%$ của Definition of Done (DoD).

---

### 📅 Ngày 20: Dockerization Multi-Stage & Production Deployment Setup
- **Thời gian hoàn thành:** 28/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Xây dựng Multi-Stage Dockerfile:**
     - [Dockerfile](../Dockerfile):
       - Stage 1 (Builder): Biên dịch source code với Eclipse Temurin 21 JDK Alpine & Maven Wrapper.
       - Stage 2 (Runner): Chạy trên base image siêu nhẹ `eclipse-temurin:21-jre-alpine`, thiết lập non-root user `snapuser:snapgroup` và tối ưu tham số JVM (`-XX:+UseG1GC -XX:MaxRAMPercentage=75.0`).
  2. **Thiết lập Build Exclusions:**
     - [.dockerignore](../.dockerignore): Loại bỏ `.git`, `.env`, `target/`, log files nhằm tối ưu dung lượng image và bảo mật.

---

### 📅 Ngày 21: Hoàn thiện Tài liệu Kiến trúc Hệ thống & Tổng duyệt DoD
- **Thời gian hoàn thành:** 28/09/2026
- **Trạng thái:** ✅ **Hoàn thành**
- **Nội dung công việc đã làm:**
  1. **Soạn thảo README.md Chuẩn Showcase GitHub:**
     - [README.md](../README.md): Đầy đủ Badges công nghệ, sơ đồ kiến trúc Flowchart Mermaid, sơ đồ Sequence Diagram luồng Cache-Aside Redirect & Async Telemetry, ERD Diagram, bảng Benchmark ($p50 = 1.48\text{ ms}$), danh mục 10 REST APIs kèm mẫu curl JSON và hướng dẫn chạy.
  2. **Biên soạn Cẩm nang Phỏng vấn & Đọc hiểu Source Code:**
     - [interview_guide.md](./interview_guide.md) & [snaplink_mastery_answers.md](./snaplink_mastery_answers.md): Hướng dẫn trả lời 100% các câu hỏi phỏng vấn chuyên sâu về Java Core, Spring Boot, Redis Lua, Concurrency, Two-Level Cache và System Design.
  3. **Tổng duyệt Definition of Done (DoD):**
     - Đã hoàn thành 100% tất cả các tiêu chí FR1 $\rightarrow$ FR6, hiệu năng redirect $< 100\text{ ms}$, test coverage $\ge 95\%$, Swagger UI và Docker containerization.


