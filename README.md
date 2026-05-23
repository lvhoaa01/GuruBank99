# Bank99 — Android Banking App for Software Testing

> Dự án Android mô phỏng hệ thống ngân hàng (Customer + Teller), dùng làm bài tập môn **Software Testing**. Tài liệu này hướng dẫn cài đặt và chạy app.


---

## 1. Yêu cầu phần mềm

Cần cài 3 phần mềm chính (tổng ~3 GB):

| Phần mềm | Phiên bản | Link tải |
|---|---|---|
| **Git** | 2.40+ | <https://git-scm.com/download/win> |
| **Android Studio** | Ladybug (2024.2) trở lên | <https://developer.android.com/studio> |
| **PostgreSQL** | 18 (hoặc 16/17) | <https://www.postgresql.org/download/windows/> |

Lưu ý khi cài:
- **Android Studio**: chọn "Standard install", sẽ tự kéo về SDK + emulator + JDK 21 (bundled).
- **PostgreSQL**: nhớ password của user `postgres` (dùng cho bước 4) và để mặc định port **5432**.
- **Git**: dùng setting mặc định khi cài.

---

## 2. Clone project từ GitHub

Mở **PowerShell** (Windows) hoặc Terminal, chạy:

```powershell
cd C:\
git clone <URL-repo-GitHub-của-nhóm> GuruBank99
cd GuruBank99
```

(Thay `<URL-repo-GitHub-của-nhóm>` bằng link repo, ví dụ `https://github.com/team-bank99/Bank99.git`.)

Sau khi xong, thư mục `C:\GuruBank99` sẽ chứa toàn bộ project.

---

## 3. Cài đặt database PostgreSQL

Database lưu data người dùng + tài khoản + giao dịch của app.

### 3.1. Tạo user + database

Mở **SQL Shell (psql)** từ Start menu (Windows). Khi hỏi:
- Server: nhấn Enter (mặc định localhost)
- Database: nhấn Enter (postgres)
- Port: nhấn Enter (5432)
- Username: nhấn Enter (postgres)
- Password: **nhập password của user `postgres`** đã đặt khi cài.

Sau khi vào được prompt `postgres=#`, copy-paste 4 dòng sau:

```sql
CREATE USER bank99user WITH PASSWORD 'bank99pass';
CREATE DATABASE bank99 OWNER bank99user;
\c bank99
GRANT ALL ON SCHEMA public TO bank99user;
```

Gõ `\q` để thoát.

### 3.2. Load schema + dữ liệu mẫu

Thao tác trên giao diện của pgAdmin4 để tiến hành load các schema theo trình tự : schema -> seed -> tellerschema... -> tellerseed....

### Test load schema
`` SELECT * FROM USER
...

## 4. Mở project trong Android Studio

1. Mở **Android Studio**.
2. Trên màn hình welcome → click **"Open"** → chọn thư mục `C:\GuruBank99` (Tùy theo path gốc lúc đầu clone) → OK.
3. Android Studio sẽ tự tải dependency (lần đầu mất ~5 phút). Theo dõi tiến trình dưới đáy IDE.
4. Khi thấy thông báo "Gradle sync finished" → sẵn sàng.

Nếu sync lỗi và yêu cầu chọn JDK: vào **File → Settings → Build → Gradle → Gradle JDK** → chọn **"Embedded JDK"** (jbr 21).

---

## 5. Tạo emulator Android

1. Trên Android Studio: click icon **Device Manager** (bên phải).
2. Click **"Create Device"** → chọn **Pixel 6** (hoặc bất kỳ phone nào).
3. Chọn **System Image**: API **30** trở lên (Android 11+). Nếu chưa tải → click "Download" cạnh API. Mất ~1 GB và 5 phút.
4. Click **Next → Finish**.
5. Click nút Play ▶ cạnh emulator vừa tạo để khởi động. Lần đầu mất 1–2 phút boot.

---

## 6. Chạy app

1. Đảm bảo:
   - Emulator đang chạy (thấy màn hình Android).
   - PostgreSQL service đang chạy (kiểm tra Start menu → Services → `postgresql-x64-18` = Running).
2. Trên thanh trên cùng Android Studio: chọn emulator vừa tạo trong dropdown.
3. Click nút **Run ▶** (hoặc nhấn `Shift + F10`).
4. Sau ~30 giây, app **Bank99** hiện trên emulator. Vào màn Login.

---

## 7. Tài khoản test có sẵn

Database đã seed sẵn 5 user (password đều giống nhau cho tiện test):

| Username | Password | Role | Mô tả |
|---|---|---|---|
| `C10001` | `Password1!` | Customer | 2 tài khoản (SAVING + CURRENT), 5M + 2M VND |
| `C10002` | `Password1!` | Customer | 1 tài khoản CURRENT, 500K VND |
| `C10003` | `Password1!` | Customer | **Bị khoá 15 phút** — dùng để test lockout |
| `T001` | `TellerPass1!` | Teller | Chi nhánh CN001 (Quận 1) |
| `T002` | `TellerPass1!` | Teller | Chi nhánh CN002 (Quận 3) |

Tài khoản ngân hàng seed sẵn:
- `9900000001` — SAVING của C10001, 5,000,000 VND
- `9900000002` — CURRENT của C10001, 2,000,000 VND
- `9900000003` — CURRENT của C10002, 500,000 VND

---
