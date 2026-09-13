package com.securebank.dsa;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable Transaction model representing a financial ledger entry.
 * Implements Comparable for natural sorting by timestamp.
 */
public class Transaction implements Comparable<Transaction> {
    private final String transactionId;
    private final String accountNumber;
    private final String type; // "DEPOSIT", "WITHDRAWAL", "TRANSFER_SENT", "TRANSFER_RECEIVED"
    private final double amount;
    private final double balanceAfter;
    private final String description;
    private final LocalDateTime timestamp;

    public Transaction(String transactionId, String accountNumber, String type, 
                       double amount, double balanceAfter, String description) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public String getTransactionId() { return transactionId; }
    public String getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public int compareTo(Transaction other) {
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] ID: %-12s | %-16s | Amt: $%8.2f | Bal: $%8.2f | %s",
                timestamp.format(dtf), transactionId, type, amount, balanceAfter, description);
    }
}
