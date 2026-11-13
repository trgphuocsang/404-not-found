# 404-not-found

# HỆ THỐNG QUẢN LÝ CỬA HÀNG BÁCH HÓA KHU DÂN CƯ

## Giới thiệu

Dự án xây dựng hệ thống quản lý cửa hàng bách hóa gồm 3 thành phần:

- **Admin App:** Android (Java + XML) – Quản lý cửa hàng, sản phẩm, kho hàng và hoạt động kinh doanh.
- **Client App:** Flutter (Dart) – Ứng dụng dành cho khách hàng, hỗ trợ tìm kiếm, mua sắm và theo dõi đơn hàng.
- **Backend:** Node.js + Express – Xử lý nghiệp vụ, xác thực, phân quyền và kết nối cơ sở dữ liệu.
- **Database:** PostgreSQL.

**Điểm nhấn:** Quản lý tồn kho theo từng lô và áp dụng nguyên tắc **FEFO (First Expired, First Out)** – ưu tiên xuất bán lô hàng có hạn sử dụng gần nhất trước.

##Cấu trúc dự án

```text
404-not-found/
│
├── admin-android/       # Ứng dụng Android Admin
│
├── client-flutter/      # Ứng dụng Flutter Client
│
├── backend/             # Backend Node.js + Express
│
├── database/            # Schema và dữ liệu mẫu
│
├── docs/                # Tài liệu phân tích, thiết kế và API
│
└── README.md
```

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Admin App | Java, XML, Retrofit 2 |
| Client App | Flutter, Dart |
| Backend | Node.js, Express |
| Database | PostgreSQL, Supabase |
| Image Storage | Supabase Storage |
| Authentication | JWT, bcrypt |
| Deployment | Render |
| Version Control | Git, GitHub |

## 👥 Phân công thành viên

| Thành viên| Nhiệm vụ |
|---|---|
| Phước Điều| Backend lõi, FEFO, hạ tầng và GitHub
| Thành Ân  | Database, Backend phụ trợ, kiểm thử và báo cáo 
| Thúy Vy   | Phát triển Flutter Client
| Khải Đông | Android Admin – Khung ứng dụng và quản lý kho 
| Phước Sang| Android Admin – Bán hàng và vận hành 

## 🌿 Quy trình Git

- Không commit trực tiếp lên `main`.
- Mỗi thành viên làm việc trên branch riêng.
- Tạo Pull Request trước khi merge vào `main`.
- Pull code mới nhất trước khi bắt đầu công việc.
- Sử dụng tài khoản GitHub cá nhân khi commit.

**Quy ước branch:**

```bash
feature/tvX-ten-chuc-nang
```

**Một số lệnh Git cơ bản:**

```bash
# Cập nhật code mới nhất
git checkout main
git pull origin main

# Tạo branch cá nhân
git checkout -b feature/tv1-auth

# Commit thay đổi
git add .
git commit -m "feat: implement authentication"

# Push branch lên GitHub
git push -u origin feature/tv1-auth
```

## ⚠️ Lưu ý
- Mỗi thành viên tối thiểu 15 commit có ý nghĩa trong quá trình thực hiện.
- Không tự ý chỉnh sửa phần code do thành viên khác phụ trách.
- Thông báo cho nhóm khi có thay đổi liên quan đến API.
- Mọi nghiệp vụ quan trọng phải được xử lý tại Backend.
