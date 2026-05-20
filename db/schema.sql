-- Bank99 — Android Customer schema for PostgreSQL
-- Drop in reverse FK order so re-running is idempotent in dev.
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    customer_id          INT          PRIMARY KEY,
    username             VARCHAR(50)  UNIQUE NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    full_name            VARCHAR(100),
    id_number            VARCHAR(12)  UNIQUE,
    date_of_birth        VARCHAR(10),
    gender               VARCHAR(10),
    address              VARCHAR(255),
    city                 VARCHAR(50),
    state                VARCHAR(50),
    pin                  VARCHAR(6),
    phone                VARCHAR(15),
    email                VARCHAR(100) UNIQUE,
    daily_limit          BIGINT       NOT NULL DEFAULT 100000000,
    status               VARCHAR(15)  NOT NULL DEFAULT 'ACTIVE',
    failed_login_count   INT          NOT NULL DEFAULT 0,
    locked_until_ms      BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE accounts (
    account_number          VARCHAR(20) PRIMARY KEY,
    owner_customer_id       INT         NOT NULL REFERENCES users(customer_id),
    account_type            VARCHAR(10) NOT NULL,
    balance                 BIGINT      NOT NULL CHECK (balance >= 0),
    active                  BOOLEAN     NOT NULL DEFAULT TRUE,
    monthly_withdraw_count  INT         NOT NULL DEFAULT 0,
    monthly_transfer_count  INT         NOT NULL DEFAULT 0,
    counters_month          INT         NOT NULL DEFAULT 0
);

CREATE TABLE transactions (
    transaction_id         BIGSERIAL    PRIMARY KEY,
    source_account         VARCHAR(20)  REFERENCES accounts(account_number),
    destination_account    VARCHAR(20)  REFERENCES accounts(account_number),
    amount                 BIGINT       NOT NULL CHECK (amount > 0),
    fee                    BIGINT       NOT NULL DEFAULT 0,
    txn_type               VARCHAR(10)  NOT NULL,
    description            VARCHAR(255),
    timestamp_ms           BIGINT       NOT NULL,
    performed_by_user_id   INT          NOT NULL,
    status                 VARCHAR(10)  NOT NULL,
    balance_after          BIGINT       NOT NULL
);

CREATE INDEX idx_txn_source ON transactions(source_account);
CREATE INDEX idx_txn_dest   ON transactions(destination_account);
CREATE INDEX idx_txn_ts     ON transactions(timestamp_ms);

-- Make sure bank99user can use the tables even when this file was loaded
-- by the postgres superuser. PG15+ no longer auto-grants on `public`.
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES    IN SCHEMA public TO bank99user;
GRANT USAGE, SELECT, UPDATE              ON ALL SEQUENCES IN SCHEMA public TO bank99user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES    TO bank99user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT, UPDATE              ON SEQUENCES TO bank99user;
