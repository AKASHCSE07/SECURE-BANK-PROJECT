package com.securebank.jdbc;

import com.securebank.exception.AccountBlockedException;
import com.securebank.exception.AccountNotFoundException;
import com.securebank.exception.BankingException;
import com.securebank.exception.InsufficientBalanceException;
import com.securebank.prototype.Account;
import com.securebank.validator.BankingValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * JDBCTransferEngine demonstrates manual ACID Transaction Management in pure Java.
 * This reveals what Spring's @Transactional does under the hood!
 *
 * ACID Workflow:
 * 1. connection.setAutoCommit(false) -> Starts Database Transaction
 * 2. Deterministic Lock Acquisition (Min Account ID -> Max Account ID) to prevent Deadlocks
 * 3. Validate Account Status and Available Balances
 * 4. Debit Source Account -> Credit Destination Account
 * 5. Insert Transaction Ledger Audit Row
 * 6. connection.commit() -> Permanently writes all changes
 * 7. On any exception: connection.rollback() -> Restores exact previous state
 * 8. finally: connection.setAutoCommit(true) & close connection
 */
public class JDBCTransferEngine {

    private final AccountDAO accountDAO;

    public JDBCTransferEngine(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public boolean executeAtomicTransfer(long sourceAccId, long destAccId, double amount, String description) {
        // Pre-validation
        BankingValidator.validatePositiveAmount(amount, "Transfer");
        if (sourceAccId == destAccId) {
            throw new BankingException("Cannot transfer to identical account ID.", "SAME_ACCOUNT_TRANSFER");
        }

        String refNumber = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.printf("%n[JDBC TRANSACTION BEGIN] Ref: %s | Transferring $%.2f from Acc #%d to Acc #%d%n",
                refNumber, amount, sourceAccId, destAccId);

        Connection conn = null;
        try {
            conn = DBConnectionManager.getConnection();
            
            // STEP 1: Disable AutoCommit to begin manual transaction boundary
            conn.setAutoCommit(false);
            System.out.println("[ACID ATOMICITY] AutoCommit disabled. Transaction boundary active.");

            // STEP 2: Deadlock-Free Canonical Lock Acquisition
            long firstLockId = Math.min(sourceAccId, destAccId);
            long secondLockId = Math.max(sourceAccId, destAccId);

            System.out.printf("[CONCURRENCY LOCK] Acquiring row lock on Account #%d (FOR UPDATE)...%n", firstLockId);
            accountDAO.findByIdForUpdate(firstLockId, conn)
                    .orElseThrow(() -> new AccountNotFoundException("ID: " + firstLockId));

            System.out.printf("[CONCURRENCY LOCK] Acquiring row lock on Account #%d (FOR UPDATE)...%n", secondLockId);
            accountDAO.findByIdForUpdate(secondLockId, conn)
                    .orElseThrow(() -> new AccountNotFoundException("ID: " + secondLockId));

            // Retrieve fresh state under lock
            Account sourceAcc = accountDAO.findByIdForUpdate(sourceAccId, conn).get();
            Account destAcc = accountDAO.findByIdForUpdate(destAccId, conn).get();

            // STEP 3: Validate Account States & Balances
            if (!"ACTIVE".equalsIgnoreCase(sourceAcc.getStatus())) {
                throw new AccountBlockedException(sourceAcc.getAccountNumber(), sourceAcc.getStatus());
            }
            if (!"ACTIVE".equalsIgnoreCase(destAcc.getStatus())) {
                throw new AccountBlockedException(destAcc.getAccountNumber(), destAcc.getStatus());
            }

            // Polymorphic withdrawal validation on source
            double sourceBalance = sourceAcc.getBalance();
            if (sourceBalance - amount < 0) { // Assuming Savings account $0 minimum for raw test
                throw new InsufficientBalanceException(sourceAcc.getAccountNumber(), amount, sourceBalance);
            }

            // STEP 4: Debit & Credit
            double newSourceBal = sourceBalance - amount;
            double newDestBal = destAcc.getBalance() + amount;

            accountDAO.updateBalance(sourceAccId, newSourceBal, conn);
            accountDAO.updateBalance(destAccId, newDestBal, conn);

            // STEP 5: Log Immutable Transaction
            accountDAO.logTransaction(refNumber, sourceAccId, destAccId, "TRANSFER", 
                    amount, newSourceBal, newDestBal, description, conn);

            // STEP 6: Commit all statements atomically
            conn.commit();
            System.out.printf("[JDBC COMMIT SUCCESS] Transaction %s committed to PostgreSQL. Sender Bal: $%.2f | Receiver Bal: $%.2f%n",
                    refNumber, newSourceBal, newDestBal);
            return true;

        } catch (Exception ex) {
            // STEP 7: Rollback on any failure
            System.err.printf("[JDBC ROLLBACK TRIGGERED] Error: %s. Rolling back all changes...%n", ex.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[ACID CONSISTENCY] Rollback successful. Database restored to prior clean state.");
                } catch (SQLException rollbackEx) {
                    System.err.println("[CRITICAL ERROR] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            // STEP 8: Clean up connection state
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restore default
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }
}
