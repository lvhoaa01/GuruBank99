# Bank99 — RTM Test Plan

**Project:** Bank99 Banking (Android, Customer + Teller scope per SRS)
**Total test cases:** 200
**Team size:** 5 (A, B, C, D, E) (ex)
**Avg load:** 40 test cases / người

---

## Phân chia nhiệm vụ (cao tầng)

| Thành viên | Khu vực phụ trách | Test count |
|---|---|---|
| **A** | Login, Logout, Lockout, Session timeout, Change Password, Security NFRs | 40 |
| **B** | New Customer, Edit Customer, Delete Customer, Auto-gen mã KH/Username/Password | 40 |
| **C** | New Account, Edit Account, Delete Account, Balance Enquiry, Account Type differentiation, Editable/Readonly fields | 40 |
| **D** | Deposit, Withdraw, Mini Statement, Customized Statement | 40 |
| **E** | Fund Transfer, OTP, Daily limit, Transaction Fees, ACID/Concurrency, NFR-Performance/Reliability/Compatibility | 40 |

Priority key — **High** = blocker / money-touching / security; **Medium** = validation logic, secondary flows; **Low** = cosmetic / NFR.

---

## A — Login, Logout, Lockout, Session, Change Password, Security NFRs

| Traceability # | Tester | Module | Business Description | Req Type | T/F | Requirement Description | Priority | TestCaseID | Detail Description |
|---|---|---|---|---|---|---|---|---|---|
| FR-A01 | A | Login | User authenticates with username + password | Functional | T99 | User-ID — không được để trống | High | TC_LOGIN_001 | Để trống User-ID → Submit → expect message "Username is required" |
| FR-A02 | A | Login | User authenticates with username + password | Functional | T100 | Password — không được để trống | High | TC_LOGIN_002 | Để trống Password → Submit → expect "Password is required" |
| FR-A03 | A | Login | User authenticates with username + password | Functional | T99/T100 | Cả hai trường trống | High | TC_LOGIN_003 | Cả 2 trường trống → Submit → expect lỗi T99 hoặc T100 |
| FR-A04 | A | Login | Authentication happy path | Functional | 3.3.5/B3 | Đúng MK → tạo session, redirect | High | TC_LOGIN_004 | Login C10001/Password1! → Dashboard mở, greeting hiển thị tên |
| FR-A05 | A | Login | Wrong credentials count failures | Functional | F50 | Sai password lần 1 → đếm fail =1 | High | TC_LOGIN_005 | C10001 / "wrong" → "Attempts left: 2" |
| FR-A06 | A | Login | Wrong credentials count failures | Functional | F50 | Sai password lần 2 → fail=2 | High | TC_LOGIN_006 | C10001 / "wrong" 2 lần liên tiếp → "Attempts left: 1" |
| FR-A07 | A | Login | Wrong credentials → lock | Functional | F50 | Sai 3 lần liên tiếp → khóa TK 15 phút | High | TC_LOGIN_007 | C10001 / "wrong" x3 → status = LOCKED, lockedUntil = now+15min |
| FR-A08 | A | Login | Locked account blocked | Functional | F50 | TK đang Locked, trong cửa sổ 15 phút | High | TC_LOGIN_008 | C10003 (seed Locked) → expect "Account is temporarily locked" |
| FR-A09 | A | Login | Auto-unlock after 15 phút | Functional | F50 | TK Locked sau khi hết 15 phút → auto unlock | High | TC_LOGIN_009 | Mock clock vượt LockedUntil → login đúng MK → SUCCESS, failedCount reset |
| FR-A10 | A | Login | Unknown username | Functional | 3.3.5/B2 | User không tồn tại trong DB | Medium | TC_LOGIN_010 | "C99999" / "x" → expect "User not found" |
| FR-A11 | A | Login | Disabled account | Functional | DB-User | Status = Disabled → từ chối | Medium | TC_LOGIN_011 | Update DB status='DISABLED' → login → "Account is disabled" |
| FR-A12 | A | Login | Successful login resets fail count | Functional | F50 | Sau khi đăng nhập thành công → fail count = 0 | High | TC_LOGIN_012 | Sai 2 lần, lần 3 đúng → status ACTIVE, failedCount = 0 |
| FR-A13 | A | Login | Boundary value: fail count = 2 vẫn ACTIVE | Functional | F50 | 2 lần sai chưa khóa | Medium | TC_LOGIN_013 | Sai 2 lần → status vẫn ACTIVE |
| FR-A14 | A | Login | Boundary value: fail count = 3 → LOCKED | Functional | F50 | Đúng ngưỡng → khóa | High | TC_LOGIN_014 | Sai lần thứ 3 → status chuyển LOCKED ngay lập tức |
| FR-A15 | A | Session | Idle timeout 5 phút | Functional | F51 | Không hoạt động 5 phút → auto logout | High | TC_SESSION_001 | Login, idle 5 min, mở Dashboard → kéo về Login + Toast "Session expired" |
| FR-A16 | A | Session | Hoạt động reset idle timer | Functional | F51 | Mỗi action reset timer | High | TC_SESSION_002 | Login, mỗi 1 phút bấm 1 nút (5 lần) → vẫn logged in sau 5 phút |
| FR-A17 | A | Session | Boundary: 4 phút 59s vẫn active | Functional | F51 | 1ms trước timeout | Medium | TC_SESSION_003 | Mock clock = idle 299_999 ms → isLoggedIn() = true |
| FR-A18 | A | Session | Boundary: 5 phút 0s → expired | Functional | F51 | Chính xác ngưỡng | High | TC_SESSION_004 | Mock clock = idle 300_000 ms → isLoggedIn() = false |
| FR-A19 | A | Logout | Manual logout clears session | Functional | 2-Customer | Bấm Logout → clear session, về Login | High | TC_LOGOUT_001 | Login → bấm Logout → Login screen, session null |
| FR-A20 | A | Logout | Sau logout, back button không re-enter Dashboard | Functional | Security | Không lộ Dashboard sau logout | Medium | TC_LOGOUT_002 | Logout → bấm back → vẫn ở Login (Dashboard không lưu trong stack) |
| FR-A21 | A | Change Password | Old password required | Functional | T104 | Old password không được trống | High | TC_CHGPW_001 | Old="", new=Aa1!aaaa → expect "T104" |
| FR-A22 | A | Change Password | New password required | Functional | T105 | New password không được trống | High | TC_CHGPW_002 | Old=old, new="" → expect "T105" |
| FR-A23 | A | Change Password | New password phải có ≥1 số | Functional | T106 | Ít nhất 1 chữ số | High | TC_CHGPW_003 | new="Abcdef!@" → expect "T106" |
| FR-A24 | A | Change Password | New password phải có ≥1 ký tự đặc biệt | Functional | T107 | Ít nhất 1 special | High | TC_CHGPW_004 | new="Abcdef12" → expect "T107" |
| FR-A25 | A | Change Password | New password ≥ 8 ký tự | Functional | T108 | Boundary length | High | TC_CHGPW_005 | new="Ab1!abc" (7 chars) → expect "T108" |
| FR-A26 | A | Change Password | Boundary: 8 chars vừa đủ | Functional | T108 | Vừa đủ 8 | High | TC_CHGPW_006 | new="Aa1!aaaa" (8 chars, có số + special) → pass T108 |
| FR-A27 | A | Change Password | Confirm password required | Functional | T109 | Confirm không trống | High | TC_CHGPW_007 | confirm="" → expect "T109" |
| FR-A28 | A | Change Password | Password mismatch | Functional | T110 | New != Confirm | High | TC_CHGPW_008 | new="Aa1!aaaa", confirm="Different" → expect "T110" |
| FR-A29 | A | Change Password | Old MK sai (Teller) | Functional | F38 | MK cũ không đúng → lỗi | High | TC_CHGPW_009 | Teller nhập old MK sai → expect "F38" |
| FR-A30 | A | Change Password | Old MK sai (Customer) | Functional | F39 | MK cũ không đúng → lỗi | High | TC_CHGPW_010 | Customer nhập old MK sai → expect "F39" |
| FR-A31 | A | Change Password | Đổi MK thành công → logout | Functional | 3.2 Output | Sau khi đổi → "Vui lòng đăng nhập lại" + back to Login | High | TC_CHGPW_011 | Đổi MK thành công → toast hiện, redirect Login |
| FR-A32 | A | Change Password | Hash MK mới ≠ hash MK cũ | Functional | Security | DB phải lưu hash mới | High | TC_CHGPW_012 | Sau khi đổi → query DB password_hash != hash cũ |
| FR-A33 | A | Change Password | Customer không đổi được MK người khác | Inverse | 3.13 | Mỗi user chỉ đổi MK của chính mình | Medium | TC_CHGPW_013 | Customer A login → API change password chỉ áp dụng cho A |
| NFR-A01 | A | Security | Mật khẩu được hash (không lưu plaintext) | Non-Functional | 3.12-Sec | bcrypt cost ≥ 12 (impl dùng SHA-256 cho course) | High | TC_NFR_SEC_001 | Query bảng users → password_hash không phải plaintext, ≥ 32 ký tự |
| NFR-A02 | A | Security | Phân quyền role được kiểm tra | Non-Functional | 3.12-Sec | Server-side check role | High | TC_NFR_SEC_002 | Customer cố mở Activity Teller-only → bị từ chối |
| NFR-A03 | A | Security | OWASP — SQL Injection | Non-Functional | 3.12-Sec | Không bị SQLi qua input | High | TC_NFR_SEC_003 | Username = `' OR 1=1 --` → vẫn không login được |
| NFR-A04 | A | Security | Session cookie HttpOnly/Secure | Non-Functional | 3.12-Sec | Cookie attributes (web spec) — Android equiv: session trong memory only | Medium | TC_NFR_SEC_004 | Session lưu trong SessionManager (RAM), không leak ra log |
| NFR-A05 | A | Performance | Login response < 5s | Non-Functional | 3.12-Perf | API login ≤ 5s | Medium | TC_NFR_PERF_001 | Đo thời gian từ submit → Dashboard mở, ≤ 5s |
| NFR-A06 | A | Usability | Thông báo lỗi rõ ràng | Non-Functional | 3.12-Usab | Error message chỉ rõ trường vi phạm | Low | TC_NFR_USAB_001 | Mỗi T/F trả về errorCode + message dễ hiểu |
| NFR-A07 | A | Login | Audit log on login | Non-Functional | F54 | Mọi login ghi audit | Medium | TC_NFR_AUDIT_001 | Login thành công → có entry trong AuditLog (action=LOGIN) |

---

## B — New Customer / Edit Customer / Delete Customer / Customer-side auto-gen codes

| Traceability # | Tester | Module | Business Description | Req Type | T/F | Requirement Description | Priority | TestCaseID | Detail Description |
|---|---|---|---|---|---|---|---|---|---|
| FR-B01 | B | New Customer | Customer name không cho phép số | Functional | T4 | Numbers not allowed | High | TC_NEWCUS_001 | Name = "John1" → expect "T4" |
| FR-B02 | B | New Customer | Customer name không cho phép ký tự đặc biệt | Functional | T5 | Special chars not allowed | High | TC_NEWCUS_002 | Name = "John@" → expect "T5" |
| FR-B03 | B | New Customer | Customer name không được trống | Functional | T6 | Must not be blank | High | TC_NEWCUS_003 | Name = "" → expect "T6" |
| FR-B04 | B | New Customer | Customer name không bắt đầu bằng space | Functional | T7 | First char != space | Medium | TC_NEWCUS_004 | Name = " John" → expect "T7" |
| FR-B05 | B | New Customer | Address không được trống | Functional | T8 | Required | High | TC_NEWCUS_005 | Address = "" → expect "T8" |
| FR-B06 | B | New Customer | Address không bắt đầu bằng space | Functional | T9 | First char != space | Medium | TC_NEWCUS_006 | Address = " 123 Main" → expect "T9" |
| FR-B07 | B | New Customer | Address không cho ký tự đặc biệt | Functional | T10 | Special chars not allowed | Medium | TC_NEWCUS_007 | Address = "123 Main!" → expect "T10" |
| FR-B08 | B | New Customer | City không cho ký tự đặc biệt | Functional | T11 | Special chars not allowed | Medium | TC_NEWCUS_008 | City = "Hà Nội!" → expect "T11" |
| FR-B09 | B | New Customer | City không được trống | Functional | T12 | Required | High | TC_NEWCUS_009 | City = "" → expect "T12" |
| FR-B10 | B | New Customer | City không cho số | Functional | T13 | Digits not allowed | Medium | TC_NEWCUS_010 | City = "Hanoi1" → expect "T13" |
| FR-B11 | B | New Customer | City không bắt đầu bằng space | Functional | T14 | First char != space | Low | TC_NEWCUS_011 | City = " Hanoi" → expect "T14" |
| FR-B12 | B | New Customer | State không cho số | Functional | T15 | Digits not allowed | Medium | TC_NEWCUS_012 | State = "North1" → expect "T15" |
| FR-B13 | B | New Customer | State không được trống | Functional | T16 | Required | High | TC_NEWCUS_013 | State = "" → expect "T16" |
| FR-B14 | B | New Customer | State không cho ký tự đặc biệt | Functional | T17 | Special chars not allowed | Medium | TC_NEWCUS_014 | State = "North!" → expect "T17" |
| FR-B15 | B | New Customer | State không bắt đầu bằng space | Functional | T17.1 | First char != space | Low | TC_NEWCUS_015 | State = " North" → expect "T17.1" |
| FR-B16 | B | New Customer | PIN không cho chữ cái | Functional | T18 | Letters not allowed | High | TC_NEWCUS_016 | PIN = "12345A" → expect "T18" |
| FR-B17 | B | New Customer | PIN không trống | Functional | T19 | Required | High | TC_NEWCUS_017 | PIN = "" → expect "T19" |
| FR-B18 | B | New Customer | PIN không cho ký tự đặc biệt | Functional | T20 | Special chars not allowed | High | TC_NEWCUS_018 | PIN = "12345!" → expect "T20" |
| FR-B19 | B | New Customer | PIN đúng 6 chữ số | Functional | T21 | Exactly 6 digits | High | TC_NEWCUS_019 | PIN = "12345" (5 chars) → "T21"; "1234567" (7) → "T21" |
| FR-B20 | B | New Customer | PIN không bắt đầu bằng space | Functional | T22 | First char != space | Medium | TC_NEWCUS_020 | PIN = " 12345" → expect "T22" |
| FR-B21 | B | New Customer | Telephone không trống | Functional | T23 | Required | High | TC_NEWCUS_021 | Phone = "" → expect "T23" |
| FR-B22 | B | New Customer | Telephone không cho ký tự đặc biệt | Functional | T24 | Special not allowed | Medium | TC_NEWCUS_022 | Phone = "0901-234" → expect "T24" |
| FR-B23 | B | New Customer | Telephone không cho chữ cái | Functional | T25 | Letters not allowed | Medium | TC_NEWCUS_023 | Phone = "0901a234" → expect "T25" |
| FR-B24 | B | New Customer | Telephone không bắt đầu bằng space | Functional | T26 | First char != space | Low | TC_NEWCUS_024 | Phone = " 0901234" → expect "T26" |
| FR-B25 | B | New Customer | Email không trống | Functional | T27 | Required | High | TC_NEWCUS_025 | Email = "" → expect "T27" |
| FR-B26 | B | New Customer | Email định dạng hợp lệ | Functional | T28 | Valid format | High | TC_NEWCUS_026 | Email = "not-an-email" → expect "T28" |
| FR-B27 | B | New Customer | Email không bắt đầu bằng space | Functional | T29 | First char != space | Low | TC_NEWCUS_027 | Email = " a@b.co" → expect "T29" |
| FR-B28 | B | New Customer | CMND/CCCD đúng 9 hoặc 12 số | Functional | T30 | 9 or 12 digits | High | TC_NEWCUS_028 | CMND 8 ký tự / 10 ký tự / 13 ký tự → all "T30"; 9 → pass; 12 → pass |
| FR-B29 | B | New Customer | CMND/CCCD không cho chữ + special | Functional | T31 | Digits only | High | TC_NEWCUS_029 | CMND = "1234A6789" → expect "T31" |
| FR-B30 | B | New Customer | Ngày cấp trong quá khứ và ≤ 15 năm | Functional | T32 | Past + not older than 15y | Medium | TC_NEWCUS_030 | NgàyCấp = ngày mai → reject; NgàyCấp = 16 năm trước → reject |
| FR-B31 | B | New Customer | Ngày sinh không phải tương lai | Functional | T33 | DOB format + past | Medium | TC_NEWCUS_031 | DOB = "31/12/2099" → reject; "01/01/2000" → pass |
| FR-B32 | B | New Customer | Mã số thuế 10 hoặc 13 số nếu có | Functional | T34 | 10 or 13 digits if present | Low | TC_NEWCUS_032 | MST = "1234" → reject; "1234567890" → pass; null → pass (DN optional) |
| FR-B33 | B | New Customer | Hạn mức ngày dương + ≤ trần | Functional | T35 | Positive, ≤ 3 tỷ | Medium | TC_NEWCUS_033 | Limit = -100 → reject; Limit = 3_000_000_001 → reject; 1_000_000 → pass |
| FR-B34 | B | New Customer | Email trùng → lỗi F21 | Functional | F21 | Email duplicate | High | TC_NEWCUS_034 | Email đã tồn tại trong DB → expect "F21" |
| FR-B35 | B | New Customer | CMND/CCCD trùng → lỗi F22 | Functional | F22 | ID number duplicate | High | TC_NEWCUS_035 | CMND trùng → expect "F22" |
| FR-B36 | B | New Customer | Happy path → MaKH, Username, Pwd tạm | Functional | 3.2 | Output thành công | High | TC_NEWCUS_036 | Input hợp lệ → response có MaKH, Username "C"+MaKH, mật khẩu tạm random 10 chars |
| FR-B37 | B | Auto-gen | MaKH bắt đầu 10001 | Functional | 3.4 | Auto-increment from 10001 | High | TC_AUTOGEN_001 | Customer đầu tiên = 10001, thứ 2 = 10002 |
| FR-B38 | B | Auto-gen | Username = "C" + MaKH | Functional | 3.4 | Format C+MaKH | High | TC_AUTOGEN_002 | MaKH=10005 → Username="C10005" |
| FR-B39 | B | Auto-gen | Mật khẩu tạm 10 ký tự complex | Functional | 3.4 | upper + lower + digit + special | High | TC_AUTOGEN_003 | Gen 100 lần → mỗi MK đều có ≥1 upper, lower, digit, special; length=10 |
| FR-B40 | B | Auto-gen | Mật khẩu tạm chỉ hiển thị 1 lần | Functional | 3.4/3.2 | Yêu cầu đổi MK đăng nhập đầu | High | TC_AUTOGEN_004 | New Customer tạo xong → MK tạm hiển thị 1 lần; reload → ẩn |
| FR-B41 | B | Edit Customer | Customer Id required (form) | Functional | T64 | Customer Id bắt buộc | High | TC_EDITCUS_001 | Customer Id = "" → expect "T64" |
| FR-B42 | B | Edit Customer | Customer Id không cho special (form) | Functional | T65 | Special chars not allowed | Medium | TC_EDITCUS_002 | CustomerId = "10001!" → expect "T65" |
| FR-B43 | B | Edit Customer | Customer Id không cho chữ (form) | Functional | T66 | Letters not allowed | Medium | TC_EDITCUS_003 | CustomerId = "abc" → expect "T66" |
| FR-B44 | B | Edit Customer | Customer Id không bắt đầu space (form) | Functional | T66.1 | First char != space | Low | TC_EDITCUS_004 | CustomerId = " 10001" → expect "T66.1" |
| FR-B45 | B | Edit Customer | Address không trống | Functional | T67 | Required | Medium | TC_EDITCUS_005 | Address = "" → expect "T67" |
| FR-B46 | B | Edit Customer | Address validation (T68-T69) | Functional | T68-T69 | Leading space + special | Medium | TC_EDITCUS_006 | " 1 Main!" → expect T68 hoặc T69 |
| FR-B47 | B | Edit Customer | City validation (T70-T73) | Functional | T70-T73 | Special, blank, digit, leading space | Medium | TC_EDITCUS_007 | Bộ 4 test case cho mỗi class |
| FR-B48 | B | Edit Customer | State validation (T74-T76.1) | Functional | T74-T76.1 | Digit, blank, special, leading space | Medium | TC_EDITCUS_008 | Bộ 4 test |
| FR-B49 | B | Edit Customer | PIN validation (T77-T81) | Functional | T77-T81 | Letters/blank/special/length/space | High | TC_EDITCUS_009 | Bộ 5 test (như T18-T22) |
| FR-B50 | B | Edit Customer | Telephone validation (T82-T85) | Functional | T82-T85 | blank/special/letters/leading space | Medium | TC_EDITCUS_010 | Bộ 4 test |
| FR-B51 | B | Edit Customer | Email validation (T86-T88) | Functional | T86-T88 | blank/format/leading space | Medium | TC_EDITCUS_011 | Bộ 3 test |
| FR-B52 | B | Edit Customer | Email trùng KH khác → F23 | Functional | F23 | Email collision | High | TC_EDITCUS_012 | Đổi email C10001 thành email C10002 → expect "F23" |
| FR-B53 | B | Edit Customer | Customer Id không hợp lệ → F24 | Functional | F24 | Invalid customer ID | High | TC_EDITCUS_013 | CustomerId = 99999 → expect "F24" |
| FR-B54 | B | Edit Customer | KH không thuộc chi nhánh Teller → F25 | Functional | F25 | Branch isolation | High | TC_EDITCUS_014 | Teller CN001 cố sửa KH CN002 → expect "F25" |
| FR-B55 | B | Edit Customer | Không sửa được trường định danh cốt lõi → F26 | Inverse | F26 | Read-only fields | High | TC_EDITCUS_015 | API PUT customer với HoTen/GioiTinh/NgaySinh/CMND mới → expect ignore hoặc 403 |
| FR-B56 | B | Delete Customer | Customer Id bắt buộc | Functional | T51 | Required | High | TC_DELCUS_001 | CustomerId = "" → expect "T51" |
| FR-B57 | B | Delete Customer | Customer Id không cho special | Functional | T52 | Special chars | Medium | TC_DELCUS_002 | "10001!" → expect "T52" |
| FR-B58 | B | Delete Customer | Customer Id không cho chữ | Functional | T53 | Letters | Medium | TC_DELCUS_003 | "abc" → expect "T53" |
| FR-B59 | B | Delete Customer | Customer Id không bắt đầu space | Functional | T53.1 | First char != space | Low | TC_DELCUS_004 | " 10001" → expect "T53.1" |
| FR-B60 | B | Delete Customer | Customer Id không hợp lệ → F27 | Functional | F27 | Not exist | High | TC_DELCUS_005 | CustomerId = 99999 → expect "F27" |
| FR-B61 | B | Delete Customer | KH còn TK active → F28 | Functional | F28 | Active accounts block delete | High | TC_DELCUS_006 | Xóa C10001 (còn 9900000001 active) → expect "F28" |
| FR-B62 | B | Delete Customer | KH không thuộc chi nhánh → F29 | Functional | F29 | Branch isolation | High | TC_DELCUS_007 | Teller CN001 xóa KH CN002 → expect "F29" |
| FR-B63 | B | Delete Customer | Happy path: KH không có TK → xóa | Functional | 3.2 | Output "Xóa khách hàng MaKH thành công" | High | TC_DELCUS_008 | KH không TK active → xóa thành công, DB row removed |

---

## C — Account Management (New/Edit/Delete Account, Balance Enquiry, Account Type, Editable Fields)

| Traceability # | Tester | Module | Business Description | Req Type | T/F | Requirement Description | Priority | TestCaseID | Detail Description |
|---|---|---|---|---|---|---|---|---|---|
| FR-C01 | C | New Account | Customer Id required | Functional | T1 | Customer Id bắt buộc | High | TC_NEWACC_001 | CustomerId = "" → expect "T1" |
| FR-C02 | C | New Account | Customer Id không special | Functional | T2 | Special chars not allowed | Medium | TC_NEWACC_002 | "10001!" → expect "T2" |
| FR-C03 | C | New Account | Customer Id không chữ | Functional | T3 | Letters not allowed | Medium | TC_NEWACC_003 | "abc" → expect "T3" |
| FR-C04 | C | New Account | Customer Id không bắt đầu space | Functional | T3.1 | First char != space | Low | TC_NEWACC_004 | " 10001" → expect "T3.1" |
| FR-C05 | C | New Account | Customer Id không tồn tại → F30 | Functional | F30 | Invalid customer | High | TC_NEWACC_005 | CustomerId = 99999 → expect "F30" |
| FR-C06 | C | New Account | Initial deposit < 500 → F31 | Functional | F31 | Min 500 VND | High | TC_NEWACC_006 | Initial = 499 → expect "F31" |
| FR-C07 | C | New Account | Boundary: deposit = 500 → pass | Functional | F31 | Exactly minimum | High | TC_NEWACC_007 | Initial = 500 → account created with balance=500 |
| FR-C08 | C | New Account | Boundary: deposit = 499 → fail | Functional | F31 | Just below minimum | High | TC_NEWACC_008 | Initial = 499 → expect "F31" |
| FR-C09 | C | New Account | KH khác chi nhánh → F32 | Functional | F32 | Branch isolation | High | TC_NEWACC_009 | Teller CN001 mở TK cho KH CN002 → expect "F32" |
| FR-C10 | C | New Account | Loại TK Saving | Functional | 3.1 | Saving option | High | TC_NEWACC_010 | LoaiTK = SAVING → DB row has type=SAVING |
| FR-C11 | C | New Account | Loại TK Current | Functional | 3.1 | Current option | High | TC_NEWACC_011 | LoaiTK = CURRENT → DB row has type=CURRENT |
| FR-C12 | C | New Account | Output thành công | Functional | 3.2 | Display SoTaiKhoan, LoaiTK, SoDu, NgayMo | High | TC_NEWACC_012 | Mở TK thành công → response chứa cả 4 trường |
| FR-C13 | C | Auto-gen | SoTaiKhoan = "99" + 10 digits | Functional | 3.4 | Account number format | High | TC_AUTOGEN_005 | TK mới tạo có pattern `^99\d{10}$` |
| FR-C14 | C | Auto-gen | SoTaiKhoan unique | Functional | 3.4 | No duplicate | High | TC_AUTOGEN_006 | Gen 1000 TK → tất cả khác nhau |
| FR-C15 | C | Auto-gen | MaChiNhanh format "CN" + 3 số | Functional | 3.4 | Branch code format | Medium | TC_AUTOGEN_007 | Seed branch → CN001, CN002 |
| FR-C16 | C | Edit Account | Account No required | Functional | T61 | Required | High | TC_EDITACC_001 | AccNo = "" → expect "T61" |
| FR-C17 | C | Edit Account | Account No không special | Functional | T62 | Special chars | Medium | TC_EDITACC_002 | "99-000001" → expect "T62" |
| FR-C18 | C | Edit Account | Account No không chữ | Functional | T63 | Letters | Medium | TC_EDITACC_003 | "9900000A01" → expect "T63" |
| FR-C19 | C | Edit Account | Account No không hợp lệ → F33 | Functional | F33 | Not exist | High | TC_EDITACC_004 | AccNo không tồn tại → expect "F33" |
| FR-C20 | C | Edit Account | TK khác chi nhánh → F34 | Functional | F34 | Branch isolation | High | TC_EDITACC_005 | Teller CN001 sửa TK CN002 → expect "F34" |
| FR-C21 | C | Edit Account | Đổi LoaiTK Saving→Current | Functional | 3.8 | LoaiTK editable | High | TC_EDITACC_006 | Đổi 9900000001 từ SAVING → CURRENT → save thành công |
| FR-C22 | C | Edit Account | Read-only: SoTaiKhoan | Inverse | 3.8 | Not editable | High | TC_EDITACC_007 | API PUT với SoTaiKhoan mới → ignored |
| FR-C23 | C | Edit Account | Read-only: MaKH | Inverse | 3.8 | Not editable | High | TC_EDITACC_008 | API PUT với MaKH mới → ignored |
| FR-C24 | C | Edit Account | Read-only: SoDu | Inverse | 3.8 | Balance only via transactions | High | TC_EDITACC_009 | API PUT balance=999999 → ignored |
| FR-C25 | C | Edit Account | Read-only: NgayMo | Inverse | 3.8 | Historical | Low | TC_EDITACC_010 | NgayMo không đổi qua Edit |
| FR-C26 | C | Edit Account | Read-only: TrangThai | Inverse | 3.8 | Only via Delete | Medium | TC_EDITACC_011 | TrangThai không đổi qua Edit |
| FR-C27 | C | Delete Account | Account No required | Functional | T48 | Required | High | TC_DELACC_001 | AccNo = "" → expect "T48" |
| FR-C28 | C | Delete Account | Account No không special | Functional | T49 | Special | Medium | TC_DELACC_002 | "99-000001" → expect "T49" |
| FR-C29 | C | Delete Account | Account No không chữ | Functional | T50 | Letters | Medium | TC_DELACC_003 | "9900000A01" → expect "T50" |
| FR-C30 | C | Delete Account | Account không hợp lệ → F35 | Functional | F35 | Not exist | High | TC_DELACC_004 | AccNo = 9900000099 → expect "F35" |
| FR-C31 | C | Delete Account | Số dư != 0 → F36 | Functional | F36 | Must be zero | High | TC_DELACC_005 | Close 9900000001 (balance=5M) → expect "F36" |
| FR-C32 | C | Delete Account | TK khác chi nhánh → F37 | Functional | F37 | Branch isolation | High | TC_DELACC_006 | Teller CN001 đóng TK CN002 → expect "F37" |
| FR-C33 | C | Delete Account | Happy path: balance = 0 → close | Functional | 3.2 | Output success | High | TC_DELACC_007 | Withdraw hết → Close → status=Closed |
| FR-C34 | C | Balance Enquiry | Account No required | Functional | T36 | Required | High | TC_BAL_001 | AccNo = "" → expect "T36" |
| FR-C35 | C | Balance Enquiry | Account No không special | Functional | T37 | Special | Medium | TC_BAL_002 | "99-1" → expect "T37" |
| FR-C36 | C | Balance Enquiry | Account No không chữ | Functional | T38 | Letters | Medium | TC_BAL_003 | "9900000A01" → expect "T38" |
| FR-C37 | C | Balance Enquiry | Teller xem TK thuộc chi nhánh | Functional | F1 | Branch view | High | TC_BAL_004 | Teller CN001 xem TK CN001 → trả số dư |
| FR-C38 | C | Balance Enquiry | TK phải tồn tại | Functional | F2 | Exists check | High | TC_BAL_005 | AccNo = 9999999999 → "Not found" |
| FR-C39 | C | Balance Enquiry | Customer chỉ xem TK mình | Functional | F3 | Self only | High | TC_BAL_006 | C10001 xem 9900000003 (của C10002) → reject |
| FR-C40 | C | Balance Enquiry | TK phải tồn tại (Customer) | Functional | F4 | Exists check | High | TC_BAL_007 | Customer query TK không tồn tại → "Not found" |
| FR-C41 | C | Acct Type | Saving: rút ≤ 5/tháng | Functional | F55/3.7 | Withdrawal cap | High | TC_ACCTTYPE_001 | Rút 5 lần OK, lần thứ 6 reject |
| FR-C42 | C | Acct Type | Saving: chuyển ≤ 10/tháng | Functional | F55/3.7 | Transfer cap | High | TC_ACCTTYPE_002 | Chuyển 10 lần OK, lần 11 reject |
| FR-C43 | C | Acct Type | Saving: số dư sau ≥ 500 | Functional | F55/3.7 | Floor | High | TC_ACCTTYPE_003 | Balance=600, rút 200 → balance sau 400 → reject |
| FR-C44 | C | Acct Type | Saving boundary: số dư sau = 500 | Functional | F55/3.7 | At floor exact | High | TC_ACCTTYPE_004 | Balance=700, rút 200 → 500 → accept |
| FR-C45 | C | Acct Type | Saving boundary: số dư sau = 499 | Functional | F55/3.7 | Below floor by 1 | High | TC_ACCTTYPE_005 | Balance=699, rút 200 → 499 → reject |
| FR-C46 | C | Acct Type | Current: không giới hạn rút | Functional | 3.7 | No cap | High | TC_ACCTTYPE_006 | Current rút 20 lần liên tiếp → all OK |
| FR-C47 | C | Acct Type | Current: không giới hạn CK | Functional | 3.7 | No cap | High | TC_ACCTTYPE_007 | Current chuyển 30 lần → all OK |
| FR-C48 | C | Acct Type | Current: balance floor = 0 | Functional | 3.7 | No min | Medium | TC_ACCTTYPE_008 | Current balance=100, rút 100 → balance=0 OK |
| FR-C49 | C | Acct Type | Saving monthly counter reset đầu tháng | Functional | 3.7 | Counter reset | High | TC_ACCTTYPE_009 | Month 5: rút 5 lần → tới month 6: rút thêm OK (counter reset) |
| FR-C50 | C | Acct Type | Lãi suất không tính (v1) | Functional | 3.7 | No interest in v1 | Low | TC_ACCTTYPE_010 | Sau 30 ngày, Saving balance không thay đổi tự động |

---

## D — Deposit / Withdraw / Mini Statement / Customized Statement

| Traceability # | Tester | Module | Business Description | Req Type | T/F | Requirement Description | Priority | TestCaseID | Detail Description |
|---|---|---|---|---|---|---|---|---|---|
| FR-D01 | D | Deposit | Account No required | Functional | T54 | Required | High | TC_DEP_001 | AccNo = "" → expect "T54" |
| FR-D02 | D | Deposit | Account No không special | Functional | T55 | Special | Medium | TC_DEP_002 | "99-1" → expect "T55" |
| FR-D03 | D | Deposit | Account No không chữ | Functional | T56 | Letters | Medium | TC_DEP_003 | "9900A1" → expect "T56" |
| FR-D04 | D | Deposit | Amount required | Functional | T57 | Required | High | TC_DEP_004 | Amount = "" → expect "T57" |
| FR-D05 | D | Deposit | Amount không special | Functional | T58 | Special | Medium | TC_DEP_005 | Amount = "1.5" → expect "T58" |
| FR-D06 | D | Deposit | Amount không chữ | Functional | T59 | Letters | Medium | TC_DEP_006 | Amount = "abc" → expect "T59" |
| FR-D07 | D | Deposit | Description required | Functional | T60 | Required | Medium | TC_DEP_007 | Description = "" → expect "T60" |
| FR-D08 | D | Deposit | TK không tồn tại → F18 | Functional | F18 | Account exists | High | TC_DEP_008 | AccNo = 9900000099 → expect "ACCOUNT_NOT_FOUND" |
| FR-D09 | D | Deposit | TK không thuộc chi nhánh → F19 | Functional | F19 | Branch isolation | High | TC_DEP_009 | Teller CN001 nộp vào TK CN002 → reject |
| FR-D10 | D | Deposit | Ghi mã Teller vào GD → F20 | Functional | F20 | Audit traceability | High | TC_DEP_010 | Sau deposit → performed_by_user_id = teller's userid trong DB |
| FR-D11 | D | Deposit | Amount > 0 (boundary) | Functional | 3.3.3 | Positive only | High | TC_DEP_011 | Amount = 0 → "AMT_NONPOSITIVE"; Amount = 1 → pass |
| FR-D12 | D | Deposit | Happy path output | Functional | 3.2 | MaGD, SoTK, SoTienNop, SoDuMoi, MaTeller, ThoiGian | High | TC_DEP_012 | Deposit 100k → response chứa cả 6 trường |
| FR-D13 | D | Deposit | TK đang inactive → reject | Functional | 3.3.3 | Active check | High | TC_DEP_013 | TK Closed → deposit → "ACCOUNT_INACTIVE" |
| FR-D14 | D | Deposit | Boundary: amount = max long | Functional | BVA | Overflow | Medium | TC_DEP_014 | Amount = Long.MAX_VALUE, balance trước = 100 → check no overflow |
| FR-D15 | D | Deposit | Customer không tự nộp → Inverse | Inverse | 3.13 | Customer KHÔNG nộp tiền mặt | High | TC_DEP_015 | Customer login cố deposit → endpoint từ chối |
| FR-D16 | D | Withdraw | Account No required | Functional | T111 | Required | High | TC_WD_001 | AccNo = "" → expect "T111" |
| FR-D17 | D | Withdraw | Account No không special | Functional | T112 | Special | Medium | TC_WD_002 | "99-1" → expect "T112" |
| FR-D18 | D | Withdraw | Account No không chữ | Functional | T113 | Letters | Medium | TC_WD_003 | "99A0001" → expect "T113" |
| FR-D19 | D | Withdraw | Amount required | Functional | T114 | Required | High | TC_WD_004 | Amount = "" → expect "T114" |
| FR-D20 | D | Withdraw | Amount không chữ | Functional | T115 | Letters | Medium | TC_WD_005 | Amount = "abc" → expect "T115" |
| FR-D21 | D | Withdraw | Amount không special | Functional | T116 | Special | Medium | TC_WD_006 | Amount = "1.5" → expect "T116" |
| FR-D22 | D | Withdraw | Description required | Functional | T117 | Required | Medium | TC_WD_007 | Description = "" → expect "T117" |
| FR-D23 | D | Withdraw | TK không tồn tại → F15 | Functional | F15 | Account exists | High | TC_WD_008 | AccNo = 9900000099 → reject |
| FR-D24 | D | Withdraw | Số dư không đủ → F16 | Functional | F16 | Insufficient funds | High | TC_WD_009 | Balance = 100, rút 500 → "INSUFFICIENT_FUNDS" |
| FR-D25 | D | Withdraw | TK khác chi nhánh → F17 | Functional | F17 | Branch isolation | High | TC_WD_010 | Teller CN001 rút TK CN002 → reject |
| FR-D26 | D | Withdraw | Boundary: balance = amount | Functional | BVA | Exact match | High | TC_WD_011 | Balance = 1000 (CURRENT), rút 1000 → OK, balance sau = 0 |
| FR-D27 | D | Withdraw | Boundary: balance = amount - 1 | Functional | BVA | Just insufficient | High | TC_WD_012 | Balance = 999, rút 1000 → reject |
| FR-D28 | D | Withdraw | Happy path output | Functional | 3.2 | MaGD, SoTK, SoTienRut, SoDuCo, MaTeller, ThoiGian | High | TC_WD_013 | Rút 100k → response đủ 6 trường |
| FR-D29 | D | Withdraw | Saving rút thứ 6/tháng → reject | Functional | F55 | Monthly cap = 5 | High | TC_WD_014 | Saving đã rút 5 lần tháng này → lần 6 reject |
| FR-D30 | D | Withdraw | Saving balance sau < 500 → reject | Functional | F55 | Floor 500 | High | TC_WD_015 | Saving 600, rút 200 → reject (balance sau 400) |
| FR-D31 | D | Withdraw | Saving monthly counter tăng đúng | Functional | F55 | Counter increment | High | TC_WD_016 | Sau mỗi rút Saving → monthlyWithdrawCount tăng +1 |
| FR-D32 | D | Withdraw | Customer không rút tự → Inverse | Inverse | 3.13 | Customer KHÔNG rút tiền mặt | High | TC_WD_017 | Customer login cố withdraw → endpoint từ chối |
| FR-D33 | D | Mini Statement | Account No required | Functional | T101 | Required | High | TC_MINI_001 | AccNo = "" → expect "T101" |
| FR-D34 | D | Mini Statement | Account No không special | Functional | T102 | Special | Medium | TC_MINI_002 | "99-1" → expect "T102" |
| FR-D35 | D | Mini Statement | Account No không chữ | Functional | T103 | Letters | Medium | TC_MINI_003 | "99A1" → expect "T103" |
| FR-D36 | D | Mini Statement | Hiển thị 5 GD gần nhất | Functional | 3.2 | Last 5 | High | TC_MINI_004 | Seed 10 txn → mini trả về đúng 5, sorted DESC by timestamp |
| FR-D37 | D | Mini Statement | Boundary: TK có <5 GD | Functional | BVA | Partial | Medium | TC_MINI_005 | TK có 3 GD → mini trả về 3, không lỗi |
| FR-D38 | D | Mini Statement | TK không hợp lệ (Teller) → F45 | Functional | F45 | Account check | High | TC_MINI_006 | AccNo không tồn tại → reject |
| FR-D39 | D | Mini Statement | Không có GD → thông báo F46 | Functional | F46 | Empty notice | Medium | TC_MINI_007 | TK mới mở chưa có GD → "No transactions" |
| FR-D40 | D | Mini Statement | Teller chỉ TK chi nhánh mình → F47 | Functional | F47 | Branch isolation | High | TC_MINI_008 | Teller CN001 query TK CN002 → reject |
| FR-D41 | D | Mini Statement | Customer TK không hợp lệ → F48 | Functional | F48 | Account check | High | TC_MINI_009 | Customer query AccNo không tồn tại → reject |
| FR-D42 | D | Mini Statement | Customer chỉ TK mình → F49 | Functional | F49 | Self only | High | TC_MINI_010 | C10001 query TK 9900000003 (của C10002) → reject |
| FR-D43 | D | Customized Statement | Account No required | Functional | T39 | Required | High | TC_CUST_001 | AccNo = "" → expect "T39" |
| FR-D44 | D | Customized Statement | Account No không chữ | Functional | T40 | Letters | Medium | TC_CUST_002 | "abc" → expect "T40" |
| FR-D45 | D | Customized Statement | Account No không special | Functional | T41 | Special | Medium | TC_CUST_003 | "99-1" → expect "T41" |
| FR-D46 | D | Customized Statement | Amount Lower Limit không special | Functional | T42 | Special | Medium | TC_CUST_004 | "1.5" → reject |
| FR-D47 | D | Customized Statement | Amount Lower Limit required | Functional | T43 | Required | High | TC_CUST_005 | Empty → "T43" |
| FR-D48 | D | Customized Statement | Amount Lower Limit không chữ | Functional | T44 | Letters | Medium | TC_CUST_006 | "abc" → expect "T44" |
| FR-D49 | D | Customized Statement | Number of Transaction không special | Functional | T45 | Special | Low | TC_CUST_007 | "5.5" → reject |
| FR-D50 | D | Customized Statement | Number of Transaction required | Functional | T46 | Required | High | TC_CUST_008 | Empty → "T46" |
| FR-D51 | D | Customized Statement | Number of Transaction không chữ | Functional | T47 | Letters | Low | TC_CUST_009 | "abc" → reject |
| FR-D52 | D | Customized Statement | TK không hợp lệ (Teller) → F40 | Functional | F40 | Account check | High | TC_CUST_010 | AccNo không tồn tại → reject |
| FR-D53 | D | Customized Statement | From > To → F41 | Functional | F41 | Range invalid | High | TC_CUST_011 | From=2026-05-20, To=2026-05-01 → reject |
| FR-D54 | D | Customized Statement | TK không hợp lệ (Customer) → F42 | Functional | F42 | Account check | High | TC_CUST_012 | Customer query bad AccNo → reject |
| FR-D55 | D | Customized Statement | From > To (Customer) → F43 | Functional | F43 | Range invalid | High | TC_CUST_013 | Customer query From > To → reject |
| FR-D56 | D | Customized Statement | Customer chỉ TK mình → F44 | Functional | F44 | Self only | High | TC_CUST_014 | C10001 query TK của C10002 → reject |
| FR-D57 | D | Customized Statement | Filter amount lower limit | Functional | 3.2 | Loại trừ GD < limit | High | TC_CUST_015 | Limit=100k, seed 50k+200k → kết quả chỉ 200k |
| FR-D58 | D | Customized Statement | Filter date range inclusive | Functional | BVA | Boundary date | Medium | TC_CUST_016 | GD đúng ngày From → bao gồm; sau To → loại |
| FR-D59 | D | Customized Statement | Max count cap | Functional | 3.2 | Limit applied | Medium | TC_CUST_017 | Seed 10 match, maxCount=3 → trả 3 |
| FR-D60 | D | Customized Statement | Hiển thị tổng số GD ở cuối | Functional | 3.2 | Count display | Low | TC_CUST_018 | UI hiển thị "Tổng: N GD" |

---

## E — Fund Transfer, OTP, Daily Limit, Fees, ACID, NFRs

| Traceability # | Tester | Module | Business Description | Req Type | T/F | Requirement Description | Priority | TestCaseID | Detail Description |
|---|---|---|---|---|---|---|---|---|---|
| FR-E01 | E | Fund Transfer | Payer Account required | Functional | T89 | Required | High | TC_TF_001 | Source = "" → expect "T89" |
| FR-E02 | E | Fund Transfer | Payer Account không special | Functional | T90 | Special | Medium | TC_TF_002 | "99-1" → expect "T90" |
| FR-E03 | E | Fund Transfer | Payer Account không chữ | Functional | T91 | Letters | Medium | TC_TF_003 | "99A1" → expect "T91" |
| FR-E04 | E | Fund Transfer | Payee Account required | Functional | T92 | Required | High | TC_TF_004 | Dest = "" → expect "T92" |
| FR-E05 | E | Fund Transfer | Payee Account không special | Functional | T93 | Special | Medium | TC_TF_005 | "99-1" → expect "T93" |
| FR-E06 | E | Fund Transfer | Payee Account không chữ | Functional | T94 | Letters | Medium | TC_TF_006 | "99A1" → expect "T94" |
| FR-E07 | E | Fund Transfer | Amount required | Functional | T95 | Required | High | TC_TF_007 | Amount = "" → expect "T95" |
| FR-E08 | E | Fund Transfer | Amount không chữ | Functional | T96 | Letters | Medium | TC_TF_008 | "abc" → expect "T96" |
| FR-E09 | E | Fund Transfer | Amount không special | Functional | T97 | Special | Medium | TC_TF_009 | "1.5" → expect "T97" |
| FR-E10 | E | Fund Transfer | Description required | Functional | T98 | Required | Medium | TC_TF_010 | Description = "" → expect "T98" |
| FR-E11 | E | Fund Transfer | TK đích không tồn tại (Teller) → F5 | Functional | F5 | Dest invalid | High | TC_TF_011 | Dest = 9900000099 → reject |
| FR-E12 | E | Fund Transfer | TK nguồn = TK đích (Teller) → F6 | Functional | F6 | Same account | High | TC_TF_012 | Src=Dst=9900000001 → reject |
| FR-E13 | E | Fund Transfer | Số dư không đủ (Teller) → F7 | Functional | F7 | Insufficient | High | TC_TF_013 | Balance=100, transfer=500 → reject |
| FR-E14 | E | Fund Transfer | TK nguồn khác chi nhánh → F8 | Functional | F8 | Branch isolation | High | TC_TF_014 | Teller CN001 transfer from TK CN002 → reject |
| FR-E15 | E | Fund Transfer | TK đích không hợp lệ (Customer) → F9 | Functional | F9 | Dest invalid | High | TC_TF_015 | Customer dest = 9900000099 → reject |
| FR-E16 | E | Fund Transfer | Src = Dst (Customer) → F10 | Functional | F10 | Same account | High | TC_TF_016 | Customer Src=Dst → reject |
| FR-E17 | E | Fund Transfer | Số dư không đủ (Customer) → F11 | Functional | F11 | Insufficient | High | TC_TF_017 | Balance=100, transfer=500 → reject |
| FR-E18 | E | Fund Transfer | TK nguồn không thuộc Customer → F12 | Functional | F12 | Ownership | High | TC_TF_018 | C10001 chuyển từ TK của C10002 → reject |
| FR-E19 | E | Fund Transfer | Vượt hạn mức ngày → F13 | Functional | F13 | Daily limit | High | TC_TF_019 | Daily limit 5M, đã CK 4.9M, CK thêm 200k → reject |
| FR-E20 | E | Fund Transfer | Boundary: tổng = daily limit | Functional | F13 BVA | At exact limit | High | TC_TF_020 | Đã CK 4.9M, CK thêm 100k = 5M → vẫn pass |
| FR-E21 | E | Fund Transfer | Boundary: tổng = daily limit + 1 | Functional | F13 BVA | Over by 1 | High | TC_TF_021 | Đã CK 4.9M, CK thêm 100_001 → reject |
| FR-E22 | E | Fund Transfer | OTP bắt buộc (Customer) → F14 | Functional | F14 | OTP required | High | TC_TF_022 | Customer transfer → phải qua màn OTP |
| FR-E23 | E | Fund Transfer | Teller KHÔNG cần OTP | Functional | 3.5 | Teller skip OTP | High | TC_TF_023 | Teller transfer → không hiển thị màn OTP |
| FR-E24 | E | Fund Transfer | Ghi NguoiThucHien | Functional | 3.3.2 | Audit trail | High | TC_TF_024 | Sau transfer → DB row có performed_by_user_id |
| FR-E25 | E | Fund Transfer | Happy path output | Functional | 3.2 | MaGD, TK nguồn, TK đích, Số tiền, Phí, Số dư còn lại, Thời gian | High | TC_TF_025 | Customer transfer thành công → response đủ 7 trường |
| FR-E26 | E | OTP | OTP 6 chữ số | Functional | 3.5 | Length = 6 | High | TC_OTP_001 | Generate 100 lần → mỗi OTP đúng 6 digit |
| FR-E27 | E | OTP | OTP hiệu lực 120s | Functional | 3.5 | TTL | High | TC_OTP_002 | Mock clock = nowMillis + 120_000 → submit → "OTP_EXPIRED" |
| FR-E28 | E | OTP | Boundary: 119s vẫn valid | Functional | 3.5 BVA | Just before expire | Medium | TC_OTP_003 | Mock clock = nowMillis + 119_999 → submit đúng → SUCCESS |
| FR-E29 | E | OTP | Boundary: 120s = expired | Functional | 3.5 BVA | Exact expiry | High | TC_OTP_004 | Mock clock = nowMillis + 120_000 → expired |
| FR-E30 | E | OTP | Sai OTP lần 1 | Functional | 3.5 | First wrong | Medium | TC_OTP_005 | Wrong code → "Attempts left: 2" |
| FR-E31 | E | OTP | Sai OTP lần 2 | Functional | 3.5 | Second wrong | Medium | TC_OTP_006 | Wrong code x2 → "Attempts left: 1" |
| FR-E32 | E | OTP | Sai OTP lần 3 → hủy | Functional | 3.5/F14 | Third = cancel | High | TC_OTP_007 | Wrong code x3 → "OTP_ATTEMPTS_EXHAUSTED", challenge cleared |
| FR-E33 | E | OTP | Sai 3 lần rồi đúng → vẫn block | Functional | 3.5 | No recovery | High | TC_OTP_008 | Wrong x3, sau đó submit đúng → vẫn "ATTEMPTS_EXHAUSTED" |
| FR-E34 | E | OTP | Resend OTP làm mới code | Functional | 3.5 | OTP cũ vô hiệu | High | TC_OTP_009 | Resend → code mới, code cũ submit → wrong |
| FR-E35 | E | OTP | Resend tối đa 3 lần | Functional | 3.5 | Max 3 resends | Medium | TC_OTP_010 | Resend lần 4 → "OTP_RESEND_LIMIT" |
| FR-E36 | E | OTP | OTP lưu hashed, không plaintext | Non-Functional | 3.5 | Hash storage | High | TC_OTP_011 | Inspect memory/DB → OTP không lưu plaintext |
| FR-E37 | E | OTP | OTP xóa sau khi xác thực | Functional | 3.5 | Cleanup | Medium | TC_OTP_012 | Sau success → pendingChallenge = null |
| FR-E38 | E | Fees | Deposit phí = 0 | Functional | 3.6 | Free | High | TC_FEE_001 | Deposit 1M → fee column = 0 |
| FR-E39 | E | Fees | Withdraw phí = 0 | Functional | 3.6 | Free | High | TC_FEE_002 | Withdraw 100k → fee = 0 |
| FR-E40 | E | Fees | Transfer nội bộ phí = 0 | Functional | 3.6 | Free in v1 | High | TC_FEE_003 | Transfer → fee = 0 |
| FR-E41 | E | Fees | Open account phí = 0 | Functional | 3.6 | Free | Medium | TC_FEE_004 | New Account → fee = 0 |
| FR-E42 | E | Fees | Balance check ≥ amount + fee | Functional | 3.6 | Future-proof | Medium | TC_FEE_005 | Mock fee=10, balance=100, transfer=100 → reject (100 < 100+10) |
| FR-E43 | E | ACID | Lỗi giữa BEGIN-COMMIT → rollback | Functional | F52 | Atomicity | High | TC_ACID_001 | Mock exception trong update đích → balance nguồn không đổi |
| FR-E44 | E | ACID | Tham vấn đồng thời 2 GD | Functional | F53 | Concurrency lock | High | TC_ACID_002 | 2 thread transfer cùng src đồng thời → kết quả nhất quán |
| FR-E45 | E | Audit | Mọi CRUD + GD ghi audit | Functional | F54 | Audit log | High | TC_ACID_003 | Mỗi action → row trong AuditLog |
| NFR-E01 | E | Performance | Balance query < 2s | Non-Functional | 3.12-Perf | API balance ≤ 2s | Medium | TC_NFR_PERF_002 | Đo balance query → ≤ 2s |
| NFR-E02 | E | Performance | Transfer < 5s | Non-Functional | 3.12-Perf | API transfer ≤ 5s | Medium | TC_NFR_PERF_003 | Đo transfer end-to-end → ≤ 5s |
| NFR-E03 | E | Performance | ≥ 1000 CCU | Non-Functional | 3.12-Perf | Concurrency | Low | TC_NFR_PERF_004 | Load test 1000 concurrent users → no crash |
| NFR-E04 | E | Reliability | Uptime ≥ 99.5% | Non-Functional | 3.12-Rel | SLA | Low | TC_NFR_REL_001 | Monitor 30 ngày → downtime ≤ 3.6h |
| NFR-E05 | E | Reliability | Tỉ lệ lỗi < 0.01% | Non-Functional | 3.12-Rel | Error rate | Low | TC_NFR_REL_002 | 10000 requests → ≤ 1 lỗi hệ thống |
| NFR-E06 | E | Compatibility | Chrome 90+ / Firefox 88+ / Edge 90+ | Non-Functional | 1.2 | Browser (Android equiv: API ≥ 26) | Medium | TC_NFR_COMP_001 | Build APK chạy thử trên emulator API 26, 30, 34 |
| NFR-E07 | E | Backup | RPO ≤ 1 giờ | Non-Functional | 3.12-Backup | Data loss | Low | TC_NFR_BACKUP_001 | Restore từ backup mới nhất → data loss window ≤ 1h |
| NFR-E08 | E | Backup | RTO ≤ 4 giờ | Non-Functional | 3.12-Backup | Recovery time | Low | TC_NFR_BACKUP_002 | Test recovery scenario → restore ≤ 4h |
| NFR-E09 | E | Inverse | KHÔNG hỗ trợ cho vay | Inverse | 1.2 | Out of scope | Low | TC_INV_001 | Không có endpoint /loan |
| NFR-E10 | E | Inverse | KHÔNG thanh toán hóa đơn | Inverse | 1.2 | Out of scope | Low | TC_INV_002 | Không có endpoint /bill-payment |

---

## Ghi chú thực thi

1. **TestCaseID format**: `TC_<MODULE>_<NNN>` để dễ trace.
2. **Test Status** mặc định = `Not Started`. Sau khi run cập nhật: `Pass / Fail / Blocked / Skipped`.
3. **Priority**: dùng dropdown trong cột H của file Excel (Low / Medium / High).
4. **Mỗi T-rule cha** (ví dụ T21 "PIN phải 6 chữ số") có thể tách thành 3 sub-cases: length=5, length=6, length=7 (BVA). Người test có quyền thêm sub-case khi viết code, chỉ cần giữ TestCaseID gốc + suffix `_a`, `_b`, `_c`.
5. **Branch coverage target**: với các T/F functional, đảm bảo mỗi test case chạy qua **một nhánh khác nhau** trong CFG của method tương ứng (`Validators`, `BusinessRules`). Tham chiếu plan file để biết cyclomatic complexity từng method.
6. **Test code mapping**:
   - `Validators*` (T-rules input) → unit test trong `app/src/test/java/.../utils/ValidatorsTest.java`.
   - `BusinessRules*` (F-rules) → `BusinessRulesTest.java`.
   - ViewModel-level → `*ViewModelTest.java` với Mockito.
   - UI end-to-end → Espresso trong `app/src/androidTest`.
7. **Tests for Teller-only modules** (New Customer / Edit Customer / Delete Customer / Edit Account / Delete Account / Deposit-Teller / Withdraw-Teller / Branch isolation F8, F17, F19, F25, F29, F32, F34, F37, F47) — hiện app chỉ implement Customer scope; những test này dùng để **trace yêu cầu SRS**, nhóm có thể đánh dấu `Skipped — out of Android scope` nếu không impl, hoặc chuyển sang test mock unit cho `BusinessRules.canX` logic.

## Phân bổ tóm tắt

| Thành viên | Số TC | Module chính |
|---|---|---|
| A | 40 | Login (14) + Session (4) + Logout (2) + Change Password (13) + Security NFRs (7) |
| B | 40 (đếm + sub) | New Customer (36) + Auto-gen Customer codes (4) + Edit Customer (15) + Delete Customer (8) |
| C | 40 (đếm + sub) | New Account (12) + Auto-gen Account/Branch (3) + Edit Account (11) + Delete Account (7) + Balance Enquiry (7) + Account Type (10) |
| D | 40 (đếm + sub) | Deposit (15) + Withdraw (17) + Mini Statement (10) + Customized Statement (18) |
| E | 40 (đếm + sub) | Fund Transfer (25) + OTP (12) + Fees (5) + ACID (3) + NFR Performance/Reliability/Backup/Inverse (10) |



