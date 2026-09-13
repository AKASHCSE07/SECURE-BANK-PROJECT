package com.securebank.jdbc;

import com.securebank.prototype.Account;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Data Access Object (DAO) Interface for Account operations.
 * Separates data persistence logic from business service logic.
 */
public interface AccountDAO {
    Optional<Account> findByAccountNumber(String accountNumber) throws SQLException;
    Optional<Account> findByIdForUpdate(long accountId, Connection connection) throws SQLException;
    void updateBalance(long accountId, double newBalance, Connection connection) throws SQLException;
    void logTransaction(String refNumber, Long sourceAccId, Long destAccId, 
                        String type, double amount, Double srcBalAfter, 
                        Double destBalAfter, String description, Connection connection) throws SQLException;
}
