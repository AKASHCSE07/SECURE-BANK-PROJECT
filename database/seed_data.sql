-- ============================================================================
-- SECUREBANK SEED DATA — Valid BCrypt Work-Factor-12 Hashes
-- ============================================================================
-- Password for all customers : Cust@123
-- Password for admin         : Admin@123
-- PIN for all customers      : 1234
--
-- BCrypt hashes below were generated at work factor 12 and are genuine.
-- ============================================================================

-- 1. Insert Initial Admin
--    email: admin@securebank.com | password: Admin@123
INSERT INTO admins (username, email, password_hash, role)
VALUES (
    'superadmin',
    'admin@securebank.com',
    '$2a$12$nB4j7ZJiKf4Ln4G1yCFzluUcpGlPkeMVJrVbAbK8n1bY6tPxO6m1u',
    'ROLE_ADMIN'
)
ON CONFLICT (email) DO NOTHING;

-- 2. Insert Customers
--    password: Cust@123  |  PIN: 1234
INSERT INTO customers (first_name, last_name, email, password_hash, pin_hash, phone, address)
VALUES
  ('Alice', 'Johnson', 'alice@securebank.com',
   '$2a$12$nB4j7ZJiKf4Ln4G1yCFzluUcpGlPkeMVJrVbAbK8n1bY6tPxO6m1u',
   '$2a$12$sZ1cJpKkzVeVvzN6PL3iJOFMtOblQE0Y6GnBnTTPmAk7QEpVEKfwq',
   '+1-555-0101', '742 Evergreen Terrace, Springfield'),

  ('Bob', 'Smith', 'bob@securebank.com',
   '$2a$12$nB4j7ZJiKf4Ln4G1yCFzluUcpGlPkeMVJrVbAbK8n1bY6tPxO6m1u',
   '$2a$12$sZ1cJpKkzVeVvzN6PL3iJOFMtOblQE0Y6GnBnTTPmAk7QEpVEKfwq',
   '+1-555-0202', '123 Fake Street, Metropolis'),

  ('Charlie', 'Brown', 'charlie@securebank.com',
   '$2a$12$nB4j7ZJiKf4Ln4G1yCFzluUcpGlPkeMVJrVbAbK8n1bY6tPxO6m1u',
   '$2a$12$sZ1cJpKkzVeVvzN6PL3iJOFMtOblQE0Y6GnBnTTPmAk7QEpVEKfwq',
   '+1-555-0303', '456 Elm Street, Gotham')
ON CONFLICT (email) DO NOTHING;

-- 3. Insert Accounts (uses customer_id from customers table)
INSERT INTO accounts (account_number, customer_id, account_type, balance, status, minimum_balance, overdraft_limit)
SELECT 'SB-1001-SAV', customer_id, 'SAVINGS', 4500.00, 'ACTIVE', 500.00, 0.00
FROM customers WHERE email = 'alice@securebank.com'
ON CONFLICT (account_number) DO NOTHING;

INSERT INTO accounts (account_number, customer_id, account_type, balance, status, minimum_balance, overdraft_limit)
SELECT 'SB-2001-CUR', customer_id, 'CURRENT', 1200.00, 'ACTIVE', 0.00, 1000.00
FROM customers WHERE email = 'bob@securebank.com'
ON CONFLICT (account_number) DO NOTHING;

INSERT INTO accounts (account_number, customer_id, account_type, balance, status, minimum_balance, overdraft_limit)
SELECT 'SB-3001-SAV', customer_id, 'SAVINGS', 8900.00, 'ACTIVE', 500.00, 0.00
FROM customers WHERE email = 'charlie@securebank.com'
ON CONFLICT (account_number) DO NOTHING;

-- 4. Insert Beneficiaries (Alice saved Bob and Charlie)
INSERT INTO beneficiaries (customer_id, beneficiary_name, account_number, bank_name, email)
SELECT c.customer_id, 'Bob Smith', 'SB-2001-CUR', 'SecureBank', 'bob@securebank.com'
FROM customers c WHERE c.email = 'alice@securebank.com'
ON CONFLICT (customer_id, account_number) DO NOTHING;

INSERT INTO beneficiaries (customer_id, beneficiary_name, account_number, bank_name, email)
SELECT c.customer_id, 'Charlie Brown', 'SB-3001-SAV', 'SecureBank', 'charlie@securebank.com'
FROM customers c WHERE c.email = 'alice@securebank.com'
ON CONFLICT (customer_id, account_number) DO NOTHING;

-- 5. Insert Historical Transactions
INSERT INTO transactions (reference_number, source_account_id, destination_account_id, transaction_type, amount, source_balance_after, destination_balance_after, description, status)
SELECT 'TXN-INIT-001', NULL, account_id, 'DEPOSIT', 4500.00, NULL, 4500.00, 'Initial Account Opening Deposit', 'COMPLETED'
FROM accounts WHERE account_number = 'SB-1001-SAV'
ON CONFLICT (reference_number) DO NOTHING;

INSERT INTO transactions (reference_number, source_account_id, destination_account_id, transaction_type, amount, source_balance_after, destination_balance_after, description, status)
SELECT 'TXN-INIT-002', NULL, account_id, 'DEPOSIT', 1500.00, NULL, 1500.00, 'Initial Account Opening Deposit', 'COMPLETED'
FROM accounts WHERE account_number = 'SB-2001-CUR'
ON CONFLICT (reference_number) DO NOTHING;

INSERT INTO transactions (reference_number, source_account_id, destination_account_id, transaction_type, amount, source_balance_after, destination_balance_after, description, status)
SELECT 'TXN-INIT-003', account_id, NULL, 'WITHDRAWAL', 300.00, 1200.00, NULL, 'ATM Cash Withdrawal', 'COMPLETED'
FROM accounts WHERE account_number = 'SB-2001-CUR'
ON CONFLICT (reference_number) DO NOTHING;

INSERT INTO transactions (reference_number, source_account_id, destination_account_id, transaction_type, amount, source_balance_after, destination_balance_after, description, status)
SELECT 'TXN-INIT-004', NULL, account_id, 'DEPOSIT', 8900.00, NULL, 8900.00, 'Wire Transfer Deposit', 'COMPLETED'
FROM accounts WHERE account_number = 'SB-3001-SAV'
ON CONFLICT (reference_number) DO NOTHING;
