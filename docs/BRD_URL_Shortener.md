# BUSINESS REQUIREMENTS DOCUMENT (BRD)
## Dự án: SnapLink — URL Shortener & Analytics Platform

**Phiên bản:** 1.1 (cập nhật định hướng Full-stack)
**Tác giả:** Nguyễn Kim Ngân
**Ngày:** 09/2026
**Loại dự án:** Personal Project — Full-stack Portfolio

> **Thay đổi so với bản 1.0:** Chốt định hướng ứng tuyển **Full-stack** (thay vì Backend thuần). Bổ sung yêu cầu frontend cụ thể, chốt tech stack, và điều chỉnh lại ưu tiên các hướng mở rộng sau MVP cho phù hợp mục tiêu này.

---

## 1. TỔNG QUAN DỰ ÁN

### 1.1 Bối cảnh
URL Shortener là một trong những hệ thống backend kinh điển được các công ty công nghệ lớn (Bitly, TinyURL) vận hành ở quy mô lớn, đồng thời cũng là dạng bài phỏng vấn system design phổ biến. Việc tự xây dựng lại hệ thống này ở quy mô nhỏ hơn cho phép thực hành các kỹ thuật cốt lõi của backend engineering (thuật toán sinh mã, caching, rate limiting, xử lý bất đồng bộ) **và** ghép nối với một frontend hoàn chỉnh để thể hiện năng lực full-stack.

### 1.2 Mục tiêu dự án
- Xây dựng một dịch vụ rút gọn URL có khả năng theo dõi (track) lượt truy cập chi tiết.
- Thể hiện năng lực xử lý các vấn đề backend nâng cao: cache-aside pattern, rate limiting, xử lý async, thiết kế thuật toán.
- **Xây dựng frontend hoàn chỉnh, trực quan** (không chỉ dừng ở Swagger UI) để demo được trải nghiệm người dùng thật khi phỏng vấn.
- Tạo ra một sản phẩm hoàn chỉnh, có thể demo trực tiếp (deploy công khai cả frontend lẫn backend) phục vụ mục đích phỏng vấn/CV cho vị trí **Full-stack**.

### 1.3 Phạm vi ngoài dự án (Out of scope)
- Không xây dựng hệ thống thanh toán/gói dịch vụ (free/premium).
- Không làm ứng dụng mobile.
- Không triển khai multi-tenant/enterprise features (custom domain, team workspace).
- Không xử lý traffic ở quy mô triệu request/giây — đây là bản mô phỏng kiến trúc, không phải hệ thống production thực thụ.
- **Không triển khai message queue thực sự (Kafka/RabbitMQ), sliding window rate limiting, hay Distributed ID Generator (Snowflake) trong phạm vi dự án này** — xem lý do và điều kiện cân nhắc lại ở mục 11.2.

---

## 2. ĐỐI TƯỢNG SỬ DỤNG (Stakeholders / User Personas)

| Persona | Mô tả | Nhu cầu chính |
|---|---|---|
| Guest User | Người dùng chưa đăng nhập | Rút gọn URL nhanh, không cần tài khoản |
| Registered User | Người dùng đã đăng ký | Quản lý danh sách link đã tạo, xem thống kê chi tiết |
| System/Admin (tuỳ chọn mở rộng) | Người vận hành | Giám sát hệ thống, khoá link vi phạm |

---

## 3. YÊU CẦU CHỨC NĂNG (Functional Requirements)

### FR1 — Rút gọn URL
- FR1.1: Người dùng nhập một URL gốc hợp lệ, hệ thống trả về short URL dạng `domain.com/{code}`.
- FR1.2: Hỗ trợ custom alias (người dùng đăng nhập được đặt code riêng, ví dụ `domain.com/ngan-cv`).
- FR1.3: Validate URL đầu vào (đúng định dạng, chặn scheme nguy hiểm như `javascript:`).
- FR1.4: Hỗ trợ đặt thời hạn hết hạn (expiration date) cho link.

### FR2 — Redirect
- FR2.1: Khi truy cập short URL, hệ thống redirect (HTTP 301/302) về URL gốc với độ trễ tối thiểu.
- FR2.2: Nếu code không tồn tại hoặc đã hết hạn → trả về trang 404 thân thiện.
- FR2.3: Mỗi lượt redirect được ghi nhận bất đồng bộ để phục vụ analytics, không chặn (block) response redirect chính.

### FR3 — Authentication & Quản lý tài khoản
- FR3.1: Đăng ký/đăng nhập bằng email + password (JWT-based).
- FR3.2: Người dùng đăng nhập xem được danh sách link mình đã tạo.
- FR3.3: Người dùng xoá/vô hiệu hoá link của chính mình.

### FR4 — Analytics
- FR4.1: Tổng số lượt click theo từng link.
- FR4.2: Biểu đồ lượt click theo thời gian (theo ngày/giờ).
- FR4.3: Phân bổ theo địa lý (quốc gia/thành phố, suy ra từ IP).
- FR4.4: Phân bổ theo thiết bị/trình duyệt (parse từ User-Agent).
- FR4.5: Top referrer (nguồn giới thiệu click, nếu có).

### FR5 — Rate Limiting
- FR5.1: Giới hạn số lượng request tạo short URL trên mỗi IP/user trong một khoảng thời gian (ví dụ 20 request/phút).
- FR5.2: Trả về HTTP 429 kèm thông báo rõ ràng khi vượt giới hạn.
- FR5.3: Sử dụng thuật toán fixed window hoặc token bucket đơn giản trên Redis — **không cần sliding window log** ở phạm vi MVP (xem mục 11.1 để biết điều kiện nâng cấp).

### FR6 — API Documentation
- FR6.1: Cung cấp Swagger/OpenAPI UI để test và tham khảo API.

### FR7 — Giao diện người dùng (Frontend) — *mới, phản ánh định hướng Full-stack*
- FR7.1: Trang chủ cho phép nhập URL và nhận short link ngay lập tức, không cần đăng nhập.
- FR7.2: Trang đăng ký/đăng nhập.
- FR7.3: Trang dashboard hiển thị danh sách link của user đã đăng nhập, kèm nút xoá/vô hiệu hoá.
- FR7.4: Trang thống kê chi tiết cho từng link, hiển thị biểu đồ (theo FR4.2–FR4.5) trực quan bằng thư viện chart.
- FR7.5: Giao diện responsive, dùng được trên cả desktop và mobile ở mức cơ bản.

---

## 4. YÊU CẦU PHI CHỨC NĂNG (Non-Functional Requirements)

| Hạng mục | Yêu cầu |
|---|---|
| Hiệu năng | Redirect endpoint phản hồi < 100ms (nhờ cache) |
| Khả năng mở rộng | Kiến trúc tách rời cache/DB, có thể scale horizontal cho tầng redirect |
| Bảo mật | Mật khẩu hash (BCrypt), JWT có thời hạn, chống SQL Injection qua JPA, cấu hình CORS đúng cho frontend gọi API |
| Độ tin cậy | Ghi nhận click không được làm mất hoặc chặn redirect chính (fail-safe async) |
| Khả năng bảo trì | Code tuân thủ layered architecture (Controller–Service–Repository), có unit test cho service logic |
| Khả năng quan sát | Log các thao tác quan trọng (tạo link, lỗi rate limit) |
| Trải nghiệm người dùng | Frontend phản hồi nhanh, có loading state rõ ràng khi gọi API, thông báo lỗi thân thiện (không hiển thị lỗi kỹ thuật thô cho người dùng) |

---

## 5. KIẾN TRÚC KỸ THUẬT (Technical Architecture)

### 5.1 Tech Stack đề xuất
- **Backend:** Java 17+, Spring Boot 3.x, Spring Security (JWT), Spring Data JPA
- **Database:** PostgreSQL (dữ liệu chính), Redis (cache + rate limiting)
- **Async processing:** Spring Event / `@Async` — **quyết định giữ nguyên, không nâng lên Kafka/RabbitMQ** trong phạm vi dự án này (xem lý do ở mục 11.2)
- **Docs:** Springdoc OpenAPI (Swagger UI)
- **Testing:** JUnit 5, Mockito
- **Deployment:** Render/Railway (backend), Vercel/Netlify (frontend)
- **Frontend (đã chốt, là một phần trọng tâm của dự án):**
  - **React + Vite** — không dùng Next.js vì không cần SEO/SSR cho use case này.
  - **React Router** — điều hướng giữa các trang (trang chủ, auth, dashboard, thống kê).
  - **Axios hoặc TanStack Query (React Query)** — gọi API, quản lý loading/cache state.
  - **Tailwind CSS** — styling nhanh, nhất quán.
  - **Recharts hoặc Chart.js** — vẽ biểu đồ click theo thời gian, phân bổ quốc gia/thiết bị (FR4.2–FR4.4).

### 5.2 Luồng xử lý chính

**Luồng tạo short URL:**
1. Client (frontend React) gửi request `POST /api/urls` kèm URL gốc.
2. Service validate URL → kiểm tra rate limit (Redis) → sinh short code (Base62 encoding từ auto-increment ID hoặc hash + collision check).
3. Lưu record vào PostgreSQL → trả về short URL cho client → frontend hiển thị kết quả.

**Luồng redirect:**
1. Client truy cập `GET /{code}` (thường là bấm trực tiếp vào short link, không qua frontend React).
2. Service kiểm tra Redis cache trước (cache-aside pattern) → nếu hit, redirect ngay.
3. Nếu cache miss → query PostgreSQL → ghi vào cache → redirect.
4. Sau khi redirect, publish một event bất đồng bộ qua `@Async` (click event) để service Analytics xử lý riêng (ghi DB, không ảnh hưởng tốc độ redirect).

### 5.3 Thuật toán sinh short code
- Phương án chính: Base62 encode của ID tự tăng (auto-increment) trong DB → đảm bảo unique tuyệt đối, không cần check collision.
- Phương án custom alias: kiểm tra tồn tại trong DB trước khi lưu (unique constraint).
- **Không dùng Distributed ID Generator (Snowflake)** ở phạm vi MVP vì hệ thống chạy trên 1 instance database duy nhất, auto-increment ID đã đủ đảm bảo tính duy nhất (xem mục 11.1).

---

## 6. THIẾT KẾ DATABASE (Sơ lược)

**Bảng `users`**
`id, email, password_hash, created_at`

**Bảng `urls`**
`id, short_code, original_url, custom_alias, user_id (nullable), expires_at, created_at, is_active`

**Bảng `click_events`**
`id, url_id, clicked_at, ip_address, country, device_type, browser, referrer`

*(Chỉ số cần thiết: index trên `short_code` để tra cứu nhanh, index trên `url_id` trong `click_events` để tổng hợp thống kê.)*

---

## 7. API ENDPOINTS (Tóm tắt)

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Đăng ký | Không |
| POST | `/api/auth/login` | Đăng nhập, trả JWT | Không |
| POST | `/api/urls` | Tạo short URL | Optional (guest hoặc user) |
| GET | `/{code}` | Redirect về URL gốc | Không |
| GET | `/api/urls` | Danh sách link của user | Có |
| DELETE | `/api/urls/{id}` | Xoá/vô hiệu hoá link | Có |
| GET | `/api/urls/{id}/analytics` | Thống kê chi tiết 1 link | Có |

---

## 8. TIÊU CHÍ HOÀN THÀNH (Definition of Done / Success Metrics)

- [ ] Toàn bộ FR1–FR7 hoạt động đúng như mô tả.
- [ ] Redirect endpoint đạt thời gian phản hồi < 100ms trên môi trường deploy thực tế.
- [ ] Có ít nhất 70% unit test coverage cho tầng Service.
- [ ] Swagger UI hoạt động và mô tả đầy đủ API.
- [ ] Deploy công khai **cả frontend lẫn backend**, có thể demo trực tiếp qua link.
- [ ] Frontend hoạt động mượt trên cả desktop và mobile ở mức cơ bản, không lỗi CORS.
- [ ] README rõ ràng: kiến trúc, cách chạy local (cả frontend + backend), điểm kỹ thuật nổi bật (để dùng khi phỏng vấn).

---

## 9. TIMELINE ĐỀ XUẤT

> Xem chi tiết theo ngày tại `plan.md`. Với việc bổ sung frontend là trọng tâm, nên cân nhắc timeline 4 tuần thay vì 3 tuần nếu chưa có kinh nghiệm React, hoặc giữ 3 tuần nếu dùng AI hỗ trợ code frontend để rút ngắn thời gian viết code (lưu ý: thời gian test/tích hợp frontend-backend không rút ngắn được nhiều dù có AI hỗ trợ).

| Tuần | Nội dung |
|---|---|
| Tuần 1 | Setup project, thiết kế DB, xây dựng Auth (JWT), CRUD cơ bản cho URL (FR1, FR3) |
| Tuần 2 | Redirect + Redis cache + Rate limiting (FR2, FR5) |
| Tuần 3 | Analytics bất đồng bộ (FR4), viết test, Swagger docs |
| Tuần 3–4 | Xây dựng frontend React (FR7), tích hợp với backend, deploy cả hai, viết README |

---

## 10. RỦI RO & PHƯƠNG ÁN XỬ LÝ

| Rủi ro | Phương án |
|---|---|
| Redis free-tier trên hosting có giới hạn dung lượng | Set TTL hợp lý cho cache, hoặc dùng in-memory cache (Caffeine) làm phương án dự phòng nếu không muốn phụ thuộc Redis ngoài |
| Thời gian hạn chế do vừa học vừa làm | Ưu tiên hoàn thành FR1, FR2, FR5 trước (đây là phần "đắt" nhất về mặt kỹ thuật); FR4 (analytics) có thể làm ở mức tối giản nếu thiếu thời gian |
| Không có kinh nghiệm Spring Security trước đó | Dành riêng 2-3 ngày đầu tuần 1 để làm quen JWT filter chain trước khi tích hợp vào project |
| CORS lỗi khi frontend React gọi API backend | Cấu hình `@CrossOrigin` hoặc `CorsConfigurationSource` đúng ngay từ đầu tuần làm frontend, tránh để tới cuối mới phát hiện |
| Dùng AI viết code frontend nhưng không hiểu sâu logic | Vẫn cần tự đọc lại và hiểu code AI sinh ra, đặc biệt phần gọi API/xử lý state — vì phỏng vấn có thể hỏi trực tiếp về code này |

---

## 11. HƯỚNG MỞ RỘNG (Future Enhancements)

Các hạng mục dưới đây **không thuộc phạm vi bản core** (mục 1.3), được liệt kê để định hướng phát triển sau khi hoàn thành MVP. **Đã chốt định hướng Full-stack — ưu tiên mục 11.4, tạm gác lại 11.1 và 11.2** (lý do chi tiết bên dưới).

### 11.1 System Design & Khả năng mở rộng — *tạm gác lại*
- **Distributed ID Generator** (kiểu Snowflake) thay cho auto-increment ID — tránh bottleneck khi mô phỏng hệ thống chạy nhiều instance.
- **Read replica** cho PostgreSQL — tách luồng đọc (redirect) và ghi (tạo link).
- Nâng cấp rate limiting từ giải thuật đơn giản sang **sliding window log** hoặc **leaky bucket**, so sánh trade-off với phương án ban đầu.
- **Circuit breaker** (Resilience4j) khi gọi service ngoài (geolocation/IP lookup) để đảm bảo fault-tolerance.

*Lý do tạm gác:* đây là các nâng cấp tinh chỉnh sâu bên trong một phần đã hoạt động đúng (rate limiting cơ bản, auto-increment ID đã đủ unique). Chúng đòi hỏi nhiều thời gian hiểu + tích hợp, nhưng khó "thấy" được giá trị khi demo trực quan — phù hợp hơn với mục tiêu Backend thuần, không phải Full-stack.

### 11.2 Event-Driven Architecture — *tạm gác lại*
- Thay `@Async` bằng **Kafka/RabbitMQ** thực sự cho việc ghi nhận click event — thể hiện tư duy decoupling, retry, backpressure.
- Thêm **dead-letter queue** cho các event xử lý lỗi (ví dụ IP lookup thất bại).

*Lý do tạm gác:* với quy mô dự án cá nhân, chạy trên 1 instance, traffic thấp, `@Async` đã đủ đáp ứng yêu cầu NFR (không chặn redirect chính). Kafka thực sự cần thiết khi có nhiều instance server chạy song song, khi không chấp nhận được mất dữ liệu event, hoặc traffic vượt quá khả năng xử lý của 1 app — những điều kiện chưa xảy ra ở đây. Việc thêm Kafka tốn thời gian setup/vận hành thêm 1 service, dễ phát sinh bug tích hợp mới ở tuần cuối, trong khi giá trị demo trực quan thấp hơn nhiều so với đầu tư vào frontend hoặc mục 11.4.

**Ghi chú:** Nếu khi phỏng vấn được hỏi "sao không dùng Kafka?", câu trả lời nên nêu rõ đây là lựa chọn có cân nhắc trade-off theo quy mô, không phải do chưa biết — và nêu được điều kiện cụ thể khi nào sẽ nâng cấp lên Kafka.

### 11.3 Bảo mật nâng cao
- **Malicious URL detection** qua Google Safe Browsing API trước khi cho phép rút gọn link.
- **CAPTCHA** cho guest user nhằm chống spam tạo link hàng loạt.
- Mở rộng audit log: ghi nhận chi tiết ai xoá/sửa link nào, thời điểm nào.

### 11.4 Tính năng cho người dùng — ✅ **ƯU TIÊN CHÍNH, làm sau khi MVP hoàn thành**
- **QR code generation** tự động cho mỗi short link (thư viện ZXing ở backend, hiển thị ảnh QR ở frontend).
- **Bulk URL shortening** — upload CSV, rút gọn hàng loạt.
- **Link-in-bio page** (dạng Linktree mini) — trang public liệt kê nhiều short link của 1 user, tuỳ chỉnh giao diện.
- **A/B testing redirect** — một short code trỏ tới nhiều URL đích, chia traffic theo tỉ lệ %.

*Lý do ưu tiên:* đây là các tính năng có tỷ lệ effort/impact tốt nhất cho mục tiêu Full-stack — thời gian làm ngắn (đặc biệt QR code, chưa tới 1 ngày), nhưng có giá trị demo trực quan cao ("wow factor" khi quét QR trực tiếp), và thể hiện rõ cả năng lực frontend lẫn backend cùng lúc (không riêng backend như 11.1/11.2). **Khuyến nghị chỉ chọn QR code + Link-in-bio trước, Bulk shortening và A/B testing làm thêm nếu còn dư thời gian.**

### 11.5 Observability — *không ưu tiên cho hướng Full-stack*
- Tích hợp **Prometheus + Grafana** để theo dõi request/giây, cache hit ratio, latency của redirect endpoint — thể hiện tư duy vận hành hệ thống thực tế.

*Lý do không ưu tiên:* phù hợp hơn với vị trí nhấn mạnh vận hành/production hoặc Backend senior, ít liên quan trực tiếp tới năng lực Full-stack đang muốn thể hiện.

### 11.6 Ưu tiên đề xuất theo mục tiêu ứng tuyển (đã cập nhật)
| Mục tiêu | Hướng nên chọn |
|---|---|
| Apply vị trí Backend thuần | 11.1 (System Design) hoặc 11.2 (Event-Driven) |
| **Apply vị trí Full-stack (đã chốt)** | **11.4 (QR code + Link-in-bio), giữ `@Async` thay vì Kafka, giữ auto-increment ID thay vì Snowflake** |
| Muốn nhấn mạnh tư duy vận hành/production | 11.5 (Observability) kết hợp 11.3 (Bảo mật) |

**Điều kiện để quay lại làm 11.1/11.2 sau này:** nếu còn dư thời gian đáng kể sau khi hoàn thành FR1–FR7 + mục 11.4, có thể thêm **một** hướng mở rộng backend (ưu tiên sliding window rate limiting vì nhỏ gọn, không cần setup thêm service, dễ giải thích trade-off) — nhưng không nên làm cả 3 (Kafka + sliding window + Snowflake ID) cùng lúc, vì mỗi tính năng thêm vào là một điểm phải giải thích trôi chảy khi phỏng vấn; hiểu sâu 1 tính năng luôn tốt hơn hiểu lưng chừng 3 tính năng.
