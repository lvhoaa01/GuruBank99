-- Bank99 — v2 migration: add Teller role, branches, audit log.
-- Idempotent: re-running is safe.
--
--   psql -h 127.0.0.1 -U postgres -d bank99 -f db\v2-teller-schema.sql

-- ===== 1. Update users =====
ALTER TABLE users ADD COLUMN IF NOT EXISTS role      VARCHAR(10) NOT NULL DEFAULT 'CUSTOMER';
ALTER TABLE users ADD COLUMN IF NOT EXISTS branch_id VARCHAR(10);

-- ===== 2. Branches =====
CREATE TABLE IF NOT EXISTS branches (
    branch_id   VARCHAR(10)  PRIMARY KEY,                  -- "CN" + 3 digits
    branch_name VARCHAR(100) NOT NULL,
    address     VARCHAR(255),
    phone       VARCHAR(15)
);

-- ===== 3. Tellers =====
CREATE TABLE IF NOT EXISTS tellers (
    teller_id INT          PRIMARY KEY,                    -- MaNV
    user_id   INT          UNIQUE NOT NULL REFERENCES users(customer_id),
    full_name VARCHAR(100) NOT NULL,
    branch_id VARCHAR(10)  NOT NULL REFERENCES branches(branch_id)
);

-- ===== 4. Audit log =====
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id         BIGSERIAL    PRIMARY KEY,
    user_id        INT,                                    -- nullable for system events
    action         VARCHAR(50)  NOT NULL,                  -- LOGIN, CREATE, UPDATE, DELETE, DEPOSIT...
    table_affected VARCHAR(50),
    before_json    TEXT,
    after_json     TEXT,
    ip_address     VARCHAR(45),
    timestamp_ms   BIGINT       NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_user_ts ON audit_logs(user_id, timestamp_ms DESC);
CREATE INDEX IF NOT EXISTS idx_audit_action  ON audit_logs(action);

-- ===== 5. FK constraint (users.branch_id → branches.branch_id) =====
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_users_branch'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT fk_users_branch FOREIGN KEY (branch_id)
            REFERENCES branches(branch_id);
    END IF;
END $$;

-- ===== 6. Grant new objects to app user =====
GRANT SELECT, INSERT, UPDATE, DELETE ON branches, tellers, audit_logs TO bank99user;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public         TO bank99user;
