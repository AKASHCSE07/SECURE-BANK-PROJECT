-- ============================================================================
-- SECUREBANK ESSENTIAL BANKING QUERIES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. CUSTOMER PORTFOLIO OVERVIEW (Customer Dashboard Query)
-- ----------------------------------------------------------------------------
SELECT 
    c.customer_id,
    c.first_name || ' ' || c.last_name AS full_name,
    c.email,
    a.account_number,
    a.account_type,
    a.balance,
    a.status,
    a.overdraft_limit,
    (a.balance + a.overdraft_limit) AS total_available_funds
FROM customers c
JOIN accounts a ON c.customer_id = a.customer_id
WHERE c.email = 'alice@securebank.com'
ORDER BY a.created_at ASC;

-- ----------------------------------------------------------------------------
-- 2. COMPLETE ACCOUNT STATEMENT WITH INFLOW/OUTFLOW MAPPING
-- ----------------------------------------------------------------------------
SELECT 
    t.transaction_id,
    t.reference_number,
    t.created_at,
    t.transaction_type,
    t.description,
    t.amount,
    CASE 
        WHEN t.destination_account_id = 1 THEN 'CREDIT (+)'
        WHEN t.source_account_id = 1 THEN 'DEBIT (-)'
        ELSE 'OTHER'
    END AS flow_direction,
    CASE 
        WHEN t.destination_account_id = 1 THEN t.destination_balance_after
        ELSE t.source_balance_after
    END AS running_balance
FROM transactions t
WHERE t.source_account_id = 1 OR t.destination_account_id = 1
ORDER BY t.created_at DESC
LIMIT 50;

-- ----------------------------------------------------------------------------
-- 3. ADMIN AGGREGATE METRICS (Bank Health & Vault Summary)
-- ----------------------------------------------------------------------------
SELECT 
    COUNT(DISTINCT c.customer_id) AS total_active_customers,
    COUNT(DISTINCT a.account_id) AS total_bank_accounts,
    SUM(a.balance) AS total_bank_vault_reserves,
    AVG(a.balance) AS average_account_balance,
    MAX(a.balance) AS highest_single_account_balance
FROM customers c
JOIN accounts a ON c.customer_id = a.customer_id
WHERE a.status = 'ACTIVE';

-- ----------------------------------------------------------------------------
-- 4. ATOMIC CONCURRENCY CONTROL: ROW-LEVEL PESSIMISTIC LOCKING
-- (Acquired in canonical ascending order of account_id to eliminate deadlocks)
-- ----------------------------------------------------------------------------
-- BEGIN TRANSACTION;
SELECT * 
FROM accounts 
WHERE account_id IN (1, 2) 
ORDER BY account_id ASC 
FOR UPDATE;

-- ----------------------------------------------------------------------------
-- 5. FRAUD VELOCITY DETECTION: Rapid High-Frequency Transaction Alert
-- (Identifies accounts with > 3 transactions within a 5-minute rolling window)
-- ----------------------------------------------------------------------------
SELECT 
    source_account_id,
    COUNT(*) AS rapid_transaction_count,
    SUM(amount) AS total_velocity_volume
FROM transactions
WHERE created_at >= NOW() - INTERVAL '5 minutes'
  AND source_account_id IS NOT NULL
GROUP BY source_account_id
HAVING COUNT(*) >= 3;
