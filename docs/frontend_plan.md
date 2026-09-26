# KẾ HOẠCH TRIỂN KHAI FRONTEND — SnapLink
## (React + Vite + Tailwind CSS + Recharts)

> **Mục tiêu:** Xây dựng một giao diện người dùng (UI/UX) hiện đại, trực quan và responsive để kết nối với Backend Spring Boot, hoàn thiện sản phẩm Full-stack Portfolio chuẩn phỏng vấn.

---

## 🛠️ 1. TECH STACK & CÔNG CỤ

| Thành phần | Công nghệ lựa chọn | Lý do sử dụng |
|---|---|---|
| **Core Framework** | **React (v18+) + Vite** | Tốc độ build siêu nhanh, chuẩn công nghiệp, nhẹ nhàng cho SPA |
| **Styling** | **Tailwind CSS** | Styling nhanh, nhất quán, dễ làm giao diện Dark/Light mode |
| **Routing** | **React Router DOM (v6+)** | Điều hướng SPA, hỗ trợ Protected Route bảo vệ trang Dashboard |
| **HTTP Client** | **Axios** | Cấu hình Base URL, Interceptor tự động gắn Bearer Token JWT |
| **State Management** | **React Context API** | Quản lý Auth State (`currentUser`, `token`, `login`, `logout`) |
| **Icons & UI Extras** | **Lucide React + Sonner / React Hot Toast** | Bộ icon hiện đại, thông báo Toast đẹp mắt khi Copy link |
| **Charts (Analytics)** | **Recharts** | Thư viện vẽ biểu đồ tương thích React số 1, trực quan và mượt mà |
| **QR Code** | **qrcode.react** | Tự động tạo mã QR cho link rút gọn |

---

## 📁 2. CẤU TRÚC THƯ MỤC DỰ ÁN FRONTEND

```text
snaplink-frontend/
├── public/
├── src/
│   ├── api/                     # Cấu hình gọi API Backend
│   │   ├── axiosClient.js       # Axios instance kèm interceptors
│   │   ├── authApi.js           # API login, register, me
│   │   ├── urlApi.js            # API create, list, delete, toggle status
│   │   └── analyticsApi.js      # API thống kê click events
│   ├── components/              # Các UI Components tái sử dụng
│   │   ├── common/              # Navbar, Footer, Button, Input, Modal, Toast
│   │   ├── home/                # UrlShortenerForm, ResultCard, QrModal
│   │   ├── dashboard/           # UrlTable, UrlTableRow, DeleteConfirmModal
│   │   └── analytics/           # MetricCard, TimeSeriesChart, DevicePieChart
│   ├── context/
│   │   └── AuthContext.jsx      # Quản lý phiên đăng nhập & thông tin User
│   ├── hooks/                   # Custom hooks (useClipboard, useAuth, v.v.)
│   ├── pages/
│   │   ├── HomePage.jsx         # Trang chủ: Rút gọn link nhanh (Guest & User)
│   │   ├── LoginPage.jsx        # Trang đăng nhập
│   │   ├── RegisterPage.jsx     # Trang đăng ký
│   │   ├── DashboardPage.jsx    # Trang quản lý link của User
│   │   ├── AnalyticsPage.jsx    # Trang xem biểu đồ thống kê cho 1 link
│   │   └── NotFoundPage.jsx     # Trang 404
│   ├── router/
│   │   └── AppRouter.jsx        # Khai báo routes + ProtectedRoute
│   ├── App.jsx
│   ├── main.jsx
│   └── index.css
├── package.json
├── tailwind.config.js
└── vite.config.js
```

---

## 📅 3. LỘ TRÌNH TRIỂN KHAI THEO TỪNG GIAI ĐOẠN

---

### 🚀 GIAI ĐOẠN 1: Setup Dự án, Design System & Authentication
- **Thời lượng ước tính:** 1–2 ngày
- **Mục tiêu:** Khởi tạo project, cấu hình giao tiếp Backend qua Axios, hoàn thiện đăng ký/đăng nhập.
- **Công việc cụ thể:**
  1. Khởi tạo project React với Vite + Tailwind CSS + Lucide Icons.
  2. Xây dựng `axiosClient.js`:
     - Tự động lấy JWT từ `localStorage` và gán vào header `Authorization: Bearer <token>`.
     - Bắt mã lỗi `401 Unauthorized` để tự động logout và chuyển hướng về `/login`.
  3. Xây dựng `AuthContext.jsx`:
     - Lưu trữ trạng thái `user`, `token`, `isAuthenticated`, `isLoading`.
     - Các hàm: `login(email, password)`, `register(email, password)`, `logout()`.
  4. Thiết kế Layout chung:
     - **Navbar:** Logo SnapLink, menu điều hướng, nút Login/Register (nếu chưa đăng nhập) hoặc Avatar + Email + nút Logout (nếu đã đăng nhập).
     - **Footer:** Thông tin bản quyền, GitHub repo, liên hệ.
  5. Xây dựng **LoginPage** & **RegisterPage**:
     - Form validate email, password rõ ràng.
     - Hiển thị Toast thông báo lỗi/thành công từ API.

---

### 🔗 GIAI ĐOẠN 2: Trang Chủ Rút Gọn Link (HomePage) & Tạo Mã QR
- **Thời lượng ước tính:** 1 ngày
- **Mục tiêu:** Cho phép người dùng (cả Guest và User đăng nhập) rút gọn URL ngay lập tức trên trang chủ.
- **Công việc cụ thể:**
  1. **Hero Section:** Tiêu đề ấn tượng, slogan giới thiệu SnapLink.
  2. **Form Rút Gọn URL:**
     - Input chính nhập URL dài với nút "Rút gọn" nổi bật.
     - Mục cài đặt nâng cao (Accordion / Toggle):
       - Nhập **Custom Alias** (kèm icon cảnh báo chỉ dành cho tài khoản đã đăng nhập).
       - Chọn **Ngày hết hạn (Expiration Date)**.
  3. **Card Kết Quả (Result Card):**
     - Hiển thị link ngắn đã tạo.
     - Nút **1-Click Copy** (kèm hiệu ứng copy thành công và âm thanh/toast).
     - Nút **Xem mã QR** (mở popup hiển thị QR code có thể tải về).
     - Nút **Xem thống kê** (nếu link thuộc về user đã đăng nhập).

---

### 📊 GIAI ĐOẠN 3: Trang Dashboard Quản Lý Link Cá Nhân
- **Thời lượng ước tính:** 1–2 ngày
- **Mục tiêu:** Quản lý toàn diện các link của tài khoản, phân trang, xoá và bật/tắt link.
- **Công việc cụ thể:**
  1. Tạo `ProtectedRoute.jsx` để bảo vệ đường dẫn `/dashboard` (chưa đăng nhập sẽ đẩy sang `/login`).
  2. **Thống kê tổng quan đầu trang:**
     - Tổng số link đã tạo.
     - Tổng lượt click trên tất cả các link.
  3. **Bảng Danh Sách Link (UrlTable):**
     - Hiển thị: Link ngắn, URL gốc (rút ngắn nếu quá dài), ngày tạo, ngày hết hạn, trạng thái (`Active` / `Inactive`).
     - Nút **Copy** nhanh link.
     - Switch **Bật/Tắt (Toggle Active)** gọi API `PATCH /api/urls/{id}/status`.
     - Nút **Xem thống kê** chuyển sang `/analytics/:id`.
     - Nút **Xoá link** (mở Modal xác nhận trước khi gọi `DELETE /api/urls/{id}`).
  4. **Phân trang (Pagination Bar):** Chuyển trang `Trang trước / Trang sau` tương thích với `PageResponse` từ Backend.

---

### 📈 GIAI ĐOẠN 4: Trang Thống Kê Chi Tiết & Biểu Đồ (AnalyticsPage)
- **Thời lượng ước tính:** 1–2 ngày (triển khai sau khi Backend xong Tuần 3)
- **Mục tiêu:** Trực quan hoá dữ liệu lượt truy cập link bằng biểu đồ sinh động.
- **Công việc cụ thể:**
  1. **Thẻ Metric nhanh (Top Cards):**
     - Tổng số lượt click.
     - Top quốc gia click nhiều nhất.
     - Thiết bị phổ biến nhất (Mobile vs Desktop).
  2. **Biểu đồ Clicks theo Thời gian (Recharts Area/Line Chart):**
     - Thể hiện xu hướng click theo từng ngày / từng giờ.
     - Tooltip hiển thị số click khi rê chuột.
  3. **Biểu đồ Phân bổ Thiết bị & Trình duyệt (Recharts Pie/Donut Chart):**
     - Tỷ lệ Mobile, Desktop, Tablet.
     - Tỷ lệ Chrome, Safari, Edge, Firefox.
  4. **Bảng Top Nguồn Giới Thiệu (Referrers):**
     - Danh sách website/nền tảng dẫn click (Facebook, Twitter/X, Google, Direct).

---

### 🎨 GIAI ĐOẠN 5: Tối Ưu UX, Responsive & Deploy
- **Thời lượng ước tính:** 1 ngày
- **Mục tiêu:** Đảm bảo trải nghiệm người dùng hoàn hảo trên mọi thiết bị và deploy công khai.
- **Công việc cụ thể:**
  1. **Responsive Testing:** Tối ưu hiển thị trên Mobile, Tablet, Desktop.
  2. **Loading States & Error Boundaries:** Skeleton loaders khi đang fetch dữ liệu, Empty states khi chưa có link nào.
  3. **Trang 404 Not Found:** Giao diện 404 thân thiện kèm nút quay về trang chủ.
  4. **Deploy:** Cấu hình deploy lên **Vercel** hoặc **Netlify**, thiết lập biến môi trường `VITE_API_BASE_URL`.
