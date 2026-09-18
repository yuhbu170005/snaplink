# KẾ HOẠCH TRIỂN KHAI — SnapLink (URL Shortener & Analytics Platform)

> Dựa trên BRD_URL_Shortener.md — chia theo 3 tuần, bóc nhỏ theo ngày để dễ bám tiến độ.

---

## TUẦN 1 — Nền móng: Setup, DB, Auth, CRUD cơ bản (FR1, FR3)

**Mục tiêu cuối tuần:** Có thể đăng ký/đăng nhập, tạo short URL (chưa cần redirect/cache), lưu DB đúng schema.

| Ngày | Công việc | Trạng thái |
|---|---|---|
| Ngày 1 | Setup project Spring Boot 3.x (Web, JPA, Security, Validation), kết nối PostgreSQL local (Docker Compose cho Postgres + Redis luôn để đỡ setup lại tuần sau). Tạo repo Git, cấu trúc package theo layered architecture (controller/service/repository/dto/entity). | ☐ |
| Ngày 2 | Thiết kế & tạo migration cho 3 bảng: `users`, `urls`, `click_events` (dùng Flyway hoặc Liquibase để dễ quản lý về sau). Thêm index trên `short_code` và `url_id`. | ☐ |
| Ngày 3–4 | Học nhanh JWT filter chain + implement Spring Security: `POST /api/auth/register`, `POST /api/auth/login`. BCrypt hash password, sinh JWT có thời hạn. (Rủi ro đã nêu trong BRD — dành hẳn 2 ngày.) | ☐ |
| Ngày 5 | `POST /api/urls`: validate URL (định dạng hợp lệ, chặn scheme `javascript:`, `data:`...), sinh short code bằng Base62 encode từ auto-increment ID, lưu DB. Hỗ trợ optional custom alias (check unique constraint). | ☐ |
| Ngày 6 | `GET /api/urls` (list theo user), `DELETE /api/urls/{id}` (xoá/vô hiệu hoá, chỉ cho chủ sở hữu). Thêm expiration date khi tạo link (FR1.4). | ☐ |
| Ngày 7 | Viết unit test cho AuthService và UrlService (build coverage ngay từ đầu). Buffer/catch-up nếu JWT ở ngày 3–4 kéo dài hơn dự kiến. | ☐ |

**Rủi ro cần theo dõi:** Nếu JWT chiếm quá nhiều thời gian, có thể tạm dùng auth đơn giản (API key hoặc session) cho bản demo đầu, quay lại hoàn thiện JWT sau — nhưng đừng bỏ hẳn vì FR3 yêu cầu rõ JWT-based.

---

## TUẦN 2 — Phần "đắt" nhất: Redirect + Redis cache + Rate limiting (FR2, FR5)

**Mục tiêu cuối tuần:** Redirect chạy đúng luồng cache-aside, có rate limit, đạt latency mục tiêu <100ms.

| Ngày | Công việc | Trạng thái |
|---|---|---|
| Ngày 8 | Implement `GET /{code}`: luồng cache-aside — check Redis trước, cache miss thì query Postgres rồi ghi lại vào Redis, redirect 301/302 về URL gốc. Xử lý case code không tồn tại/hết hạn → trang 404 thân thiện. | ☐ |
| Ngày 9 | Đo latency thực tế (JMeter/k6 hoặc curl + time), set TTL hợp lý cho cache. Chuẩn bị sẵn fallback Caffeine in-memory cache nếu cần (rủi ro Redis free-tier). | ☐ |
| Ngày 10–11 | Rate limiting: dùng Redis (token bucket hoặc fixed window đơn giản trước) để giới hạn request tạo short URL theo IP/user (ví dụ 20 req/phút). Trả HTTP 429 kèm message rõ ràng khi vượt giới hạn. Test bằng cách spam request. | ☐ |
| Ngày 12 | Viết unit test cho phần cache-aside logic và rate limiter (mock Redis nếu cần). | ☐ |
| Ngày 13 | Bắt đầu phần async click tracking (chuẩn bị cho tuần 3): dùng Spring `@Async` + event, publish event ngay sau khi redirect nhưng **không** chờ xử lý xong mới trả response. | ☐ |
| Ngày 14 | Buffer + review lại toàn bộ luồng redirect end-to-end, đảm bảo đạt NFR <100ms trên môi trường gần giống production. | ☐ |

**Điểm mấu chốt kỹ thuật cần thể hiện tốt:** cache-aside pattern đúng chuẩn, và rate limiting không làm chậm luồng redirect chính.

---

## TUẦN 3 — Analytics, Test, Docs, Deploy (FR4, FR6, DoD)

**Mục tiêu cuối tuần:** Sản phẩm hoàn chỉnh, deploy công khai, đủ test coverage, có README để dùng khi phỏng vấn.

| Ngày | Công việc | Trạng thái |
|---|---|---|
| Ngày 15 | Hoàn thiện async click event listener: ghi vào `click_events` (IP, thời gian, referrer). Parse User-Agent để lấy device/browser (FR4.4) — dùng thư viện có sẵn (ví dụ ua-parser). | ☐ |
| Ngày 16 | Geolocation từ IP (FR4.3) — dùng service/thư viện free (ví dụ GeoLite2 offline database để tránh phụ thuộc API ngoài). Nếu thiếu thời gian, làm tối giản theo BRD. | ☐ |
| Ngày 17 | `GET /api/urls/{id}/analytics`: tổng click (FR4.1), biểu đồ theo ngày/giờ (FR4.2 — trả dữ liệu dạng time-series), top referrer (FR4.5). | ☐ |
| Ngày 18 | Tích hợp Springdoc OpenAPI (Swagger UI) cho toàn bộ API, mô tả rõ ràng từng endpoint, request/response schema (FR6.1). | ☐ |
| Ngày 19 | Chốt unit test, đảm bảo đạt ≥70% coverage tầng Service (dùng JaCoCo để đo). Fix bug phát sinh. | ☐ |
| Ngày 20 | Deploy backend lên Render/Railway, cấu hình biến môi trường (DB, Redis, JWT secret). Nếu có frontend tối giản, deploy lên Vercel/Netlify. Test lại toàn bộ flow trên môi trường thật, đo lại latency redirect thực tế. | ☐ |
| Ngày 21 | Viết README: kiến trúc hệ thống (kèm diagram), hướng dẫn chạy local, các điểm kỹ thuật nổi bật (cache-aside, async event, rate limiting, Base62 encoding). Review lại checklist DoD. | ☐ |

---

## Checklist Definition of Done (từ BRD mục 8)

- [ ] Toàn bộ FR1–FR6 hoạt động đúng như mô tả.
- [ ] Redirect endpoint đạt thời gian phản hồi < 100ms trên môi trường deploy thực tế.
- [ ] Có ít nhất 70% unit test coverage cho tầng Service.
- [ ] Swagger UI hoạt động và mô tả đầy đủ API.
- [ ] Deploy công khai, có thể demo trực tiếp qua link.
- [ ] README rõ ràng: kiến trúc, cách chạy local, điểm kỹ thuật nổi bật.

---

## Gợi ý mở rộng sau MVP (BRD mục 11)

Chỉ bắt đầu sau khi hoàn thành MVP (tuần 1–3). Chọn 1–2 hướng phù hợp mục tiêu apply, không làm dàn trải:

| Mục tiêu | Hướng nên chọn |
|---|---|
| Backend thuần | Sliding Window rate limiting, hoặc chuyển sang Kafka/RabbitMQ cho click event |
| Full-stack | QR code generation + Link-in-bio page (ROI cao, dễ demo trực quan) |
| Nhấn mạnh vận hành/production | Prometheus + Grafana (ấn tượng với nhà tuyển dụng backend senior) |
