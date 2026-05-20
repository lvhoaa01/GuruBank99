-- Bank99 — v2 seed: 2 branches, 2 Teller users, assign existing customers to branches.
-- Password "TellerPass1!" hashed as SHA-256("bank99::TellerPass1!"):
--   27063438a22da0881cc5074008b4e0e26b7d54477bb40478cdf608d4c777427d
--
-- Run AFTER v2-teller-schema.sql.

-- ===== Branches =====
INSERT INTO branches (branch_id, branch_name, address, phone) VALUES
 ('CN001', 'Chi nhanh Quan 1', '12 Le Loi, Quan 1, TPHCM',  '0281234567'),
 ('CN002', 'Chi nhanh Quan 3', '34 Vo Van Tan, Quan 3, TPHCM', '0287654321')
ON CONFLICT (branch_id) DO UPDATE
    SET branch_name = EXCLUDED.branch_name,
        address     = EXCLUDED.address,
        phone       = EXCLUDED.phone;

-- ===== Teller users (UserID 1, 2 — separate range from customers starting at 10001) =====
INSERT INTO users (customer_id, username, password_hash, full_name, id_number,
                   pin, role, branch_id,
                   daily_limit, status, failed_login_count, locked_until_ms)
VALUES
 (1, 'T001',
  '27063438a22da0881cc5074008b4e0e26b7d54477bb40478cdf608d4c777427d',
  'Pham Quang Huy', 'NV0000001', '111111', 'TELLER', 'CN001',
  0, 'ACTIVE', 0, 0),
 (2, 'T002',
  '27063438a22da0881cc5074008b4e0e26b7d54477bb40478cdf608d4c777427d',
  'Le Thi Mai', 'NV0000002', '222222', 'TELLER', 'CN002',
  0, 'ACTIVE', 0, 0)
ON CONFLICT (customer_id) DO UPDATE
    SET role          = EXCLUDED.role,
        branch_id     = EXCLUDED.branch_id,
        password_hash = EXCLUDED.password_hash;

-- ===== Tellers (employee rows) =====
INSERT INTO tellers (teller_id, user_id, full_name, branch_id) VALUES
 (1, 1, 'Pham Quang Huy', 'CN001'),
 (2, 2, 'Le Thi Mai',     'CN002')
ON CONFLICT (teller_id) DO UPDATE
    SET full_name = EXCLUDED.full_name,
        branch_id = EXCLUDED.branch_id;

-- ===== Assign existing customers to branches for isolation tests =====
UPDATE users SET branch_id = 'CN001', role = 'CUSTOMER' WHERE customer_id = 10001;
UPDATE users SET branch_id = 'CN002', role = 'CUSTOMER' WHERE customer_id = 10002;
UPDATE users SET branch_id = 'CN001', role = 'CUSTOMER' WHERE customer_id = 10003;
