package com.securebank.jdbc;

import com.securebank.prototype.Account;
import com.securebank.prototype.CurrentAccount;
import com.securebank.prototype.SavingsAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Pure JDBC Implementation of AccountDAO.
 * Demonstrates:
 * 1. Parameterized PreparedStatement (Zero SQL Injection).
 * 2. ResultSet mapping to polymorphic domain objects (SavingsAccount vs CurrentAccount).
 * 3. Pessimistic Row Locking via "FOR UPDATE".
 */
public class AccountDAOImpl implements AccountDAO {

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT account_id, account_number, customer_id, account_type, balance, status, " +
                     "minimum_balance, overdraft_limit FROM accounts WHERE account_number = ?";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, accountNumber); // Parameterized SQL injection defense

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Account> findByIdForUpdate(long accountId, Connection connection) throws SQLException {
        // Enforces Pessimistic Row-Level Lock
        String sql = "SELECT account_id, account_number, customer_id, account_type, balance, status, " +
                     "minimum_balance, overdraft_limit FROM accounts WHERE account_id = ? FOR UPDATE";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void updateBalance(long accountId, double newBalance, Connection connection) throws SQLException {
        String sql = "UPDATE accounts SET balance = ?, updated_at = CURRENT_TIMESTAMP WHERE account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setLong(2, accountId);
            ps.executeUpdate();
        }
    }

    @Override
    public void logTransaction(String refNumber, Long sourceAccId, Long destAccId, 
                               String type, double amount, Double srcBalAfter, 
                               Double destBalAfter, String description, Connection connection) throws SQLException {
        String sql = "INSERT INTO transactions (reference_number, source_account_id, destination_account_id, " +
                     "transaction_type, amount, source_balance_after, destination_balance_after, description, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'COMPLETED')";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, refNumber);
            if (sourceAccId != null) ps.setLong(2, sourceAccId); else ps.setNull(2, java.sql.Types.BIGINT);
            if (destAccId != null) ps.setLong(3, destAccId); else ps.setNull(3, java.sql.Types.BIGINT);
            ps.setString(4, type);
            ps.setDouble(5, amount);
            if (srcBalAfter != null) ps.setDouble(6, srcBalAfter); else ps.setNull(6, java.sql.Types.DECIMAL);
            if (destBalAfter != null) ps.setDouble(7, destBalAfter); else ps.setNull(7, java.sql.Types.DECIMAL);
            ps.setString(8, description);
            ps.executeUpdate();
        }
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        String accNo = rs.getString("account_number");
        String custId = String.valueOf(rs.getLong("customer_id"));
        String type = rs.getString("account_type");
        double balance = rs.getDouble("balance");
        String status = rs.getString("status");
        double minBalance = rs.getDouble("minimum_balance");
        double overdraft = rs.getDouble("overdraft_limit");

        Account account;
        if ("SAVINGS".equalsIgnoreCase(type)) {
            account = new SavingsAccount(accNo, custId, balance, 0.04);
        } else {
            account = new CurrentAccount(accNo, custId, balance, overdraft);
        }
        account.setStatus(status);
        return account;
    }
}
