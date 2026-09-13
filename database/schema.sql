-- ============================================================================
-- SECUREBANK RELATIONAL DATABASE SCHEMA (PostgreSQL DDL)
-- ============================================================================
-- Enforces:
-- 1. Referential Integrity (Foreign Keys with RESTRICT on delete)
-- 2. Domain Constraints (CHECK balance >= 0, amount > 0, valid ENUM types)
-- 3. High Performance B-Tree Indexes for O(log n) lookups
-- ============================================================================

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS beneficiaries CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS admins CASCADE;
DROP TABLE IF EXISTS customers CASCADE;

-- ----------------------------------------------------------------------------
-- 1. TABLE: CUSTOMERS
-- ----------------------------------------------------------------------------
CREATE TABLE customers (
    customer_id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    pin_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    address TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ----------------------------------------------------------------------------
-- 2. TABLE: ACCOUNTS
-- ----------------------------------------------------------------------------
CREATE TABLE accounts (
    account_id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00 NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    minimum_balance DECIMAL(15, 2) DEFAULT 500.00 NOT NULL,
    overdraft_limit DECIMAL(15, 2) DEFAULT 0.00 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Foreign Key Constraint
    CONSTRAINT fk_accounts_customer 
        FOREIGN KEY (customer_id) 
        REFERENCES customers(customer_id) 
        ON DELETE RESTRICT,

    -- Domain Check Constraints
    CONSTRAINT chk_account_type 
        CHECK (account_type IN ('SAVINGS', 'CURRENT')),
        
    CONSTRAINT chk_account_status 
        CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED')),
        
    CONSTRAINT chk_positive_balance 
        CHECK (balance + overdraft_limit >= 0)
);

-- ----------------------------------------------------------------------------
-- 3. TABLE: TRANSACTIONS
-- ----------------------------------------------------------------------------
CREATE TABLE transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    reference_number VARCHAR(50) NOT NULL UNIQUE,
    source_account_id BIGINT,
    destination_account_id BIGINT,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    source_balance_after DECIMAL(15, 2),
    destination_balance_after DECIMAL(15, 2),
    description VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'COMPLETED' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Foreign Key Constraints
    CONSTRAINT fk_txn_source_acc 
        FOREIGN KEY (source_account_id) 
        REFERENCES accounts(account_id) 
        ON DELETE RESTRICT,

    CONSTRAINT fk_txn_dest_acc 
        FOREIGN KEY (destination_account_id) 
        REFERENCES accounts(account_id) 
        ON DELETE RESTRICT,

    -- Domain Constraints
    CONSTRAINT chk_txn_type 
        CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'INTEREST_CREDIT')),

    CONSTRAINT chk_txn_status 
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED')),

    CONSTRAINT chk_positive_amount 
        CHECK (amount > 0.00)
);

-- ----------------------------------------------------------------------------
-- 4. TABLE: BENEFICIARIES
-- ----------------------------------------------------------------------------
CREATE TABLE beneficiaries (
    beneficiary_id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    beneficiary_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    bank_name VARCHAR(100) DEFAULT 'SecureBank' NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_beneficiary_customer 
        FOREIGN KEY (customer_id) 
        REFERENCES customers(customer_id) 
        ON DELETE CASCADE,

    -- Prevent adding the same beneficiary account number twice for a customer
    CONSTRAINT uk_customer_beneficiary_acc 
        UNIQUE (customer_id, account_number)
);

-- ----------------------------------------------------------------------------
-- 5. TABLE: ADMINS
-- ----------------------------------------------------------------------------
CREATE TABLE admins (
    admin_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) DEFAULT 'ROLE_ADMIN' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ----------------------------------------------------------------------------
-- 6. TABLE: AUDIT_LOGS
-- ----------------------------------------------------------------------------
CREATE TABLE audit_logs (
    log_id BIGSERIAL PRIMARY KEY,
    actor_type VARCHAR(20) NOT NULL, -- 'CUSTOMER', 'ADMIN', 'SYSTEM'
    actor_id BIGINT,
    action VARCHAR(100) NOT NULL,    -- 'LOGIN', 'FREEZE_ACCOUNT', 'TRANSFER'
    ip_address VARCHAR(45),
    details JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ============================================================================
-- HIGH-PERFORMANCE B-TREE INDEXES
-- ============================================================================
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_transactions_source_acc ON transactions(source_account_id);
CREATE INDEX idx_transactions_dest_acc ON transactions(destination_account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_beneficiaries_customer_id ON beneficiaries(customer_id);
CREATE INDEX idx_audit_actor ON audit_logs(actor_type, actor_id);
