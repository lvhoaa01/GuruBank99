-- Seed data for Bank99 (Customer-only Android app).
-- Password "Password1!" hashed as SHA-256 of ("bank99::" + password):
--   874df0ad2bc5c93f9149684b8b4bee3a94061cc716a4b66427dbef47473f3530

-- Reset any prior data while keeping schema in place.
TRUNCATE transactions RESTART IDENTITY CASCADE;
DELETE FROM accounts;
DELETE FROM users;

-- ===== Users =====
INSERT INTO users (customer_id, username, password_hash, full_name, id_number,
                   date_of_birth, gender, address, city, state, pin,
                   phone, email, daily_limit, status,
                   failed_login_count, locked_until_ms)
VALUES
 (10001, 'C10001',
  '874df0ad2bc5c93f9149684b8b4bee3a94061cc716a4b66427dbef47473f3530',
  'Nguyen Van A', '123456789',  '01/01/1990', 'Nam',
  '1 Main St', 'Hanoi', 'North', '123456',
  '0901234567', 'a@example.com', 50000000, 'ACTIVE', 0, 0),

 (10002, 'C10002',
  '874df0ad2bc5c93f9149684b8b4bee3a94061cc716a4b66427dbef47473f3530',
  'Tran Thi B',  '123456790',  '02/02/1992', 'Nu',
  '2 Main St', 'Hanoi', 'North', '123456',
  '0901234568', 'b@example.com', 20000000, 'ACTIVE', 0, 0),

 -- C10003 starts LOCKED for 15 minutes from now so testers can hit the F50 path.
 (10003, 'C10003',
  '874df0ad2bc5c93f9149684b8b4bee3a94061cc716a4b66427dbef47473f3530',
  'Le Van C',    '123456791',  '03/03/1995', 'Nam',
  '3 Main St', 'Hanoi', 'North', '123456',
  '0901234569', 'c@example.com', 10000000, 'LOCKED', 3,
  (EXTRACT(EPOCH FROM NOW())::BIGINT * 1000) + 900000);

-- ===== Accounts =====
INSERT INTO accounts (account_number, owner_customer_id, account_type, balance, active,
                      monthly_withdraw_count, monthly_transfer_count, counters_month)
VALUES
 ('9900000001', 10001, 'SAVING',  5000000, TRUE, 0, 0, 0),
 ('9900000002', 10001, 'CURRENT', 2000000, TRUE, 0, 0, 0),
 ('9900000003', 10002, 'CURRENT',  500000, TRUE, 0, 0, 0);

-- ===== Transactions =====
-- Timestamps are "now - N days" computed in PostgreSQL itself for portability.
WITH base AS (
    SELECT (EXTRACT(EPOCH FROM NOW())::BIGINT * 1000) AS now_ms,
           (24 * 60 * 60 * 1000)::BIGINT             AS day_ms
)
INSERT INTO transactions
    (source_account, destination_account, amount, fee, txn_type,
     description, timestamp_ms, performed_by_user_id, status, balance_after)
SELECT * FROM (VALUES
    (NULL,         '9900000001', 1000000::BIGINT, 0::BIGINT, 'DEPOSIT',
     'Initial deposit',  (SELECT now_ms - 30 * day_ms FROM base), 10001, 'SUCCESS', 6000000::BIGINT),
    (NULL,         '9900000001',  500000::BIGINT, 0::BIGINT, 'DEPOSIT',
     'Salary',           (SELECT now_ms - 25 * day_ms FROM base), 10001, 'SUCCESS', 6500000::BIGINT),
    ('9900000001', NULL,          200000::BIGINT, 0::BIGINT, 'WITHDRAW',
     'ATM withdrawal',   (SELECT now_ms - 20 * day_ms FROM base), 10001, 'SUCCESS', 6300000::BIGINT),
    ('9900000001', '9900000003',  100000::BIGINT, 0::BIGINT, 'TRANSFER',
     'Lunch',            (SELECT now_ms - 18 * day_ms FROM base), 10001, 'SUCCESS', 6200000::BIGINT),
    (NULL,         '9900000002',  300000::BIGINT, 0::BIGINT, 'DEPOSIT',
     'Bonus',            (SELECT now_ms - 12 * day_ms FROM base), 10001, 'SUCCESS', 2300000::BIGINT),
    ('9900000002', NULL,          100000::BIGINT, 0::BIGINT, 'WITHDRAW',
     'Coffee',           (SELECT now_ms -  8 * day_ms FROM base), 10001, 'SUCCESS', 2200000::BIGINT),
    (NULL,         '9900000003',  150000::BIGINT, 0::BIGINT, 'DEPOSIT',
     'Cash deposit',     (SELECT now_ms -  5 * day_ms FROM base), 10002, 'SUCCESS',  650000::BIGINT),
    ('9900000003', '9900000001',   50000::BIGINT, 0::BIGINT, 'TRANSFER',
     'Repayment',        (SELECT now_ms -  3 * day_ms FROM base), 10002, 'SUCCESS',  600000::BIGINT),
    (NULL,         '9900000001',   20000::BIGINT, 0::BIGINT, 'DEPOSIT',
     'Round-up',         (SELECT now_ms -  1 * day_ms FROM base), 10001, 'SUCCESS', 6270000::BIGINT),
    ('9900000001', NULL,           10000::BIGINT, 0::BIGINT, 'WITHDRAW',
     'Snack',            (SELECT now_ms -      1 FROM base),     10001, 'SUCCESS', 6260000::BIGINT)
) AS t(source_account, destination_account, amount, fee, txn_type,
       description, timestamp_ms, performed_by_user_id, status, balance_after);
