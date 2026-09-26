# TÀI LIỆU ĐẶC TẢ CHI TIẾT GIAO DIỆN FRONTEND (UI/UX SPECIFICATION)
## Dự án: SnapLink — URL Shortener & Analytics Platform

> Tài liệu này mô tả chi tiết quy chuẩn thiết kế, luồng tương tác, component và API mapping của từng màn hình để phục vụ quá trình dựng giao diện Frontend.

---

## 🎨 1. DESIGN SYSTEM & QUY CHUẨN THIẾT KẾ

### 1.1 Bảng Màu (Color Palette)
- **Primary / Brand:** `Indigo-600` (`#4F46E5`) $\rightarrow$ `Violet-600` (`#7C3AED`) (Gradient thương hiệu chính).
- **Secondary / Success:** `Emerald-500` (`#10B981`) (Trạng thái Active, Copy thành công, Metric tăng trưởng).
- **Warning / Expired:** `Amber-500` (`#F59E0B`) (Cảnh báo link sắp hết hạn).
- **Danger / Error:** `Rose-500` (`#F43F5E`) (Nút xoá, thông báo lỗi, link bị vô hiệu hoá).
- **Neutral & Backgrounds (Modern Dark & Light):**
  - Light mode: Nền `Slate-50` (`#F8FAFC`), Card `White` (`#FFFFFF`), Border `Slate-200`.
  - Dark mode: Nền `Slate-950` (`#020617`), Card `Slate-900/80` (`#0F172A`), Border `Slate-800`.
  - Hiệu ứng: **Glassmorphism** (`backdrop-blur-md`, viền `border-slate-700/50`).

### 1.2 Typography & Icon
- **Font gia đình:** `Inter`, `Outfit` hoặc `system-ui, sans-serif`.
- **Bộ Icon:** `Lucide React` (`Link`, `Copy`, `Check`, `QrCode`, `BarChart2`, `Trash2`, `ExternalLink`, `ShieldCheck`, `Calendar`, `Eye`, `EyeOff`, `LogOut`, `User`).

---

## 📱 2. ĐẶC TẢ CHI TIẾT TỪNG MÀN HÌNH (PAGE SPECIFICATIONS)

---

### 🏠 MÀN HÌNH 1: TRANG CHỦ (HomePage — `/`)

#### 🎯 Mục đích:
Cho phép cả **Guest (khách vãng lai)** và **User đã đăng nhập** nhập URL dài và nhận ngay short link trong 1 giây.

```text
+-----------------------------------------------------------------------------------+
|  [Logo SnapLink]                                       [Dashboard]  [Avatar/Login]|
|                                                                                   |
|                   Rút Gọn Link Thông Minh, Theo Dõi Toàn Diện                     |
|            Biến những liên kết dài dòng thành đường dẫn ngắn gọn và an toàn.       |
|                                                                                   |
|  +-----------------------------------------------------------------------------+  |
|  | [Icon Link]  https://your-very-long-url-here.com/...         [Rút gọn ngay] |  |
|  +-----------------------------------------------------------------------------+  |
|                                                                                   |
|  [v] Tuỳ chọn nâng cao (Custom Alias & Thời hạn)                                  |
|  +-----------------------------------------------------------------------------+  |
|  | Custom Alias: [ snaplink.io/ ] [ ngan-cv ]     Hạn: [ 2026-10-01 ]          |  |
|  +-----------------------------------------------------------------------------+  |
|                                                                                   |
|  +-----------------------------------------------------------------------------+  |
|  |  Kết quả của bạn:                                                           |  |
|  |  https://snaplink.io/ngan-cv             [📋 Copy Link]  [📱 Mã QR]  [📊 Thống kê]|  |
|  |  Link gốc: https://linkedin.com/in/ngankim...                               |  |
|  +-----------------------------------------------------------------------------+  |
+-----------------------------------------------------------------------------------+
```

#### 🧩 Các Component & Trạng thái:
1. **Hero Header:** Tiêu đề lớn có chữ gradient, phụ đề mô tả tính năng.
2. **Form Rút Gọn (UrlInputForm):**
   - Input chính: Có icon `Link2`, placeholder *"Dán đường dẫn cần rút gọn tại đây (https://...)"*, validate định dạng URL trước khi gửi.
   - Nút Submit: Chuyển sang trạng thái `Loading Spinner` khi đang gọi API.
   - Accordion "Tuỳ chọn nâng cao":
     - `customAlias`: Input có prefix `domain.com/`. Nếu là Guest $\rightarrow$ hiển thị tooltip/badge *"Chỉ dành cho tài khoản đã đăng nhập"*.
     - `expiresAt`: Input chọn ngày giờ (DateTime Picker), chỉ cho chọn thời gian trong tương lai.
3. **Card Kết Quả (ResultCard):**
   - Hiển thị khi API trả về kết quả thành công.
   - Nút **Copy**: Click đổi sang icon `Check` màu xanh trong 2 giây + hiển thị Toast thông báo.
   - Nút **Mã QR**: Mở Modal hiển thị QR Code (dùng `qrcode.react`) + nút "Tải mã QR (PNG)".
   - Nút **Thống kê**: Nếu user đã đăng nhập, link trực tiếp sang trang `/analytics/:id`.

---

### 🔐 MÀN HÌNH 2 & 3: ĐĂNG NHẬP & ĐĂNG KÝ (`/login` & `/register`)

#### 🎯 Mục đích:
Cung cấp form xác thực tài khoản bảo mật bằng JWT.

```text
+------------------------------------------------------------+
|                       [Logo SnapLink]                      |
|                     Chào mừng trở lại                      |
|               Đăng nhập để quản lý link của bạn            |
|                                                            |
|  Email:                                                    |
|  [ email@domain.com                                      ] |
|                                                            |
|  Mật khẩu:                                                 |
|  [ **********                                      [👁️] ]  |
|                                                            |
|  [                 Đăng Nhập Ngay                        ] |
|                                                            |
|  Chưa có tài khoản? [Đăng ký miễn phí]                     |
+------------------------------------------------------------+
```

#### 🧩 Chi tiết Form:
- **Validate Client-side:**
  - Email: Không được trống, đúng định dạng `@`.
  - Password: Tối thiểu 6 ký tự.
- **Xử lý tương tác:**
  - Nút ẩn/hiện mật khẩu (toggle eye icon).
  - Bắt lỗi từ server: Nếu backend trả về 400 (sai mật khẩu/trùng email) $\rightarrow$ hiển thị Alert Box màu đỏ nổi bật.
  - Sau khi login/register thành công $\rightarrow$ lưu `token` vào `localStorage`, cập nhật `AuthContext` và redirect thẳng vào `/dashboard`.

---

### 📊 MÀN HÌNH 4: QUẢN LÝ LINK CÁ NHÂN (DashboardPage — `/dashboard`)

#### 🎯 Mục đích:
Quản lý toàn bộ danh sách liên kết mà tài khoản đã tạo, xem trạng thái và phân quyền thao tác. *(Bảo vệ bởi `ProtectedRoute`)*.

```text
+-----------------------------------------------------------------------------------------------+
| [Logo SnapLink]                                               [+ Tạo Link Mới]  [User Avatar] |
|                                                                                               |
|  +---------------------------+  +---------------------------+  +---------------------------+  |
|  | Tổng Số Link: 12          |  | Tổng Lượt Click: 1,420    |  | Link Đang Hoạt Động: 10   |  |
|  +---------------------------+  +---------------------------+  +---------------------------+  |
|                                                                                               |
|  DANH SÁCH LIÊN KẾT                                                                           |
|  +-----------------------------------------------------------------------------------------+  |
|  | Mã Ngắn          | Link Gốc                  | Ngày Tạo    | Hạn Dùng   | Status | Action  |  |
|  +------------------+---------------------------+-------------+------------+--------+---------+  |
|  | snaplink.io/cv   | https://drive.google...   | 20/09/2026  | Không hạn  | Active | [📋][📊][🗑️]|
|  | snaplink.io/sale | https://shopee.vn/item... | 21/09/2026  | 30/09/2026 | Active | [📋][📊][🗑️]|
|  | snaplink.io/7x9w | https://medium.com/@dev...| 22/09/2026  | Không hạn  | Inact  | [📋][📊][🗑️]|
|  +-----------------------------------------------------------------------------------------+  |
|                                                                                               |
|  [ << Trang Trước ]                  Trang 1 / 3                       [ Trang Sau >> ]       |
+-----------------------------------------------------------------------------------------------+
```

#### 🧩 Các Component & Thao tác:
1. **Metric Cards:** 3 thẻ tổng quan trên cùng (Tổng link, Tổng lượt click, Link active).
2. **Bảng Danh Sách (UrlTable):**
   - Cột **Short Code**: Bấm vào để mở link trong tab mới.
   - Cột **Original URL**: Cắt ngắn có dấu `...` nếu link quá dài, hover hiện tooltip full URL.
   - Cột **Status**: Switch (hoặc Badge) có thể nhấp để gọi API `PATCH /api/urls/{id}/status` đổi trạng thái tức thì.
   - Cột **Actions**:
     - 📋 **Copy**: Copy short link nhanh vào clipboard.
     - 📊 **Analytics**: Chuyển hướng sang `/analytics/:id`.
     - 🗑️ **Delete**: Mở Modal xác nhận: *"Bạn có chắc chắn muốn xoá link này? Hành động này không thể hoàn tác."* $\rightarrow$ Gọi API `DELETE /api/urls/{id}`.
3. **Phân trang (Pagination Bar):** Điều khiển `page` và `size`, tự động disable nút khi ở trang đầu/trang cuối.

---

### 📈 MÀN HÌNH 5: THỐNG KÊ CHI TIẾT & BIỂU ĐỒ (AnalyticsPage — `/analytics/:id`)

#### 🎯 Mục đích:
Trực quan hoá dữ liệu phân tích chi tiết của 1 đường link cụ thể (chuẩn bị cho API Tuần 3).

```text
+-----------------------------------------------------------------------------------------------+
| [<- Quay lại Dashboard]       Thống Kê Cho: snaplink.io/ngan-cv                               |
|                                                                                               |
|  [ Tổng Clicks: 850 ]  [ Quốc Gia Top 1: VN (78%) ]  [ Thiết Bị: Mobile (65%) ]               |
|                                                                                               |
|  +-----------------------------------------------------------------------------------------+  |
|  | BIỂU ĐỒ LƯỢT CLICK THEO THỜI GIAN (Recharts Area Chart)                                 |  |
|  |   Clicks                                                                                |  |
|  |    150 |             /\                                                                 |  |
|  |    100 |     /\     /  \    /\                                                          |  |
|  |     50 | ___/  \___/    \__/  \____                                                     |  |
|  |        +---------------------------                                                     |  |
|  |          18/9  19/9  20/9  21/9  22/9                                                   |  |
|  +-----------------------------------------------------------------------------------------+  |
|                                                                                               |
|  +---------------------------------------+  +----------------------------------------------+  |
|  | PHÂN BỔ THIẾT BỊ (Pie Chart)          |  | TOP NGUỒN TRUY CẬP (Referrers)               |  |
|  |   📱 Mobile: 65%                      |  |   1. Direct / Zalo: 450 clicks               |  |
|  |   💻 Desktop: 30%                     |  |   2. Facebook: 280 clicks                    |  |
|  |   📟 Tablet: 5%                       |  |   3. Google: 120 clicks                      |  |
|  +---------------------------------------+  +----------------------------------------------+  |
+-----------------------------------------------------------------------------------------------+
```

---

## 🔌 3. BẢNG ÁNH XẠ API BACKEND VÀO GIAO DIỆN (API MAPPING)

| Màn hình | Thao tác trên UI | Method | Endpoint Backend | Request Body / Query Params |
|---|---|:---:|---|---|
| **Trang chủ** | Tạo short link (Guest/User) | `POST` | `/api/urls` | `{ originalUrl, customAlias?, expiresAt? }` |
| **Đăng ký** | Submit form đăng ký | `POST` | `/api/auth/register` | `{ email, password }` |
| **Đăng nhập** | Submit form đăng nhập | `POST` | `/api/auth/login` | `{ email, password }` |
| **Navbar/Auth** | Lấy thông tin user hiện tại | `GET` | `/api/auth/me` | *Header: `Authorization: Bearer <token>`* |
| **Dashboard** | Load bảng danh sách link | `GET` | `/api/urls` | `?page=0&size=10&sort=createdAt,desc` |
| **Dashboard** | Xoá 1 liên kết | `DELETE` | `/api/urls/{id}` | *None (PathVariable `id`)* |
| **Dashboard** | Bật/tắt trạng thái link | `PATCH` | `/api/urls/{id}/status` | *None (PathVariable `id`)* |
| **Analytics** | Lấy dữ liệu biểu đồ link | `GET` | `/api/urls/{id}/analytics` | *(Sẽ xây dựng trong Tuần 3 Backend)* |

---

## 🚀 4. DANH SÁCH PACKAGES CẦN CÀI ĐẶT

```bash
# Khởi tạo React Vite
npm create vite@latest frontend -- --template react

# Cài đặt dependencies chính
cd frontend
npm install react-router-dom axios lucide-react qrcode.react recharts clsx tailwind-merge

# Cài đặt Tailwind CSS
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```
