package com.securebank.jdbc;

import com.securebank.exception.BankingException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnectionManager manages PostgreSQL JDBC database connections.
 * In production or Spring Boot, a Connection Pool (like HikariCP) is used.
 * Here we demonstrate raw JDBC Connection lifecycle management.
 */
public class DBConnectionManager {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/securebank_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "postgres"; // Configurable environment parameter

    static {
        try {
            // Explicitly load PostgreSQL JDBC Driver
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[JDBC WARNING] PostgreSQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    /**
     * Obtains a fresh database connection.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
