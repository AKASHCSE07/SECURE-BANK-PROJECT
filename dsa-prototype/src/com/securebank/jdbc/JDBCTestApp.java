package com.securebank.jdbc;

import com.securebank.prototype.Account;

import java.sql.SQLException;
import java.util.Optional;

/**
 * JDBCTestApp demonstrates Phase 7 JDBC persistence and ACID transaction rollback.
 */
public class JDBCTestApp {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("      SECUREBANK: PHASE 7 - JDBC & ATOMIC TRANSACTION ENGINE      ");
        System.out.println("=================================================================\n");

        AccountDAO accountDAO = new AccountDAOImpl();
        JDBCTransferEngine transferEngine = new JDBCTransferEngine(accountDAO);

        // TEST 1: SQL Injection Defense Demonstration
        System.out.println(">>> 1. TESTING SQL INJECTION DEFENSE VIA PREPAREDSTATEMENT <<<");
        String maliciousInput = "' OR '1'='1";
        System.out.println("Querying with malicious input: " + maliciousInput);
        try {
            Optional<Account> hackedAccount = accountDAO.findByAccountNumber(maliciousInput);
            if (hackedAccount.isEmpty()) {
                System.out.println("[SECURITY PASS] PreparedStatement treated input as a literal string. Zero injection occurred.");
            }
        } catch (SQLException e) {
            System.out.println("[DB INFO] " + e.getMessage());
        }

        // TEST 2: Successful Atomic Transfer (Account #1 -> Account #2)
        System.out.println("\n>>> 2. EXECUTING ATOMIC ACID TRANSFER ($300.00) <<<");
        try {
            boolean success = transferEngine.executeAtomicTransfer(1, 2, 300.00, "Monthly apartment maintenance");
            System.out.println("Transfer Result: " + (success ? "APPROVED & COMMITTED" : "REJECTED & ROLLED BACK"));
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }

        // TEST 3: Insufficient Funds Transfer (Triggers Programmatic Rollback)
        System.out.println("\n>>> 3. EXECUTING OVER-BUDGET TRANSFER ($99,999.00 -> TRIGGERS ROLLBACK) <<<");
        try {
            boolean success = transferEngine.executeAtomicTransfer(1, 2, 99999.00, "Luxury yacht purchase attempt");
            System.out.println("Transfer Result: " + (success ? "APPROVED & COMMITTED" : "REJECTED & ROLLED BACK"));
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }

        System.out.println("\n=================================================================");
        System.out.println("                PHASE 7 EXECUTION COMPLETE                       ");
        System.out.println("=================================================================");
    }
}
