package com.securebank.io;

import com.securebank.dsa.Transaction;
import com.securebank.exception.BankingException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * LedgerFileManager handles persistent ledger storage using Java NIO & File I/O.
 * Demonstrates:
 * 1. Try-with-resources (AutoCloseable) for zero file handle leaks.
 * 2. UTF-8 character encoding.
 * 3. Append-only ledger writes (StandardOpenOption.APPEND).
 * 4. CSV serialization and deserialization of Transaction objects.
 */
public class LedgerFileManager {

    private static final String CSV_HEADER = "TransactionId,AccountNumber,Type,Amount,BalanceAfter,Description,Timestamp";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final Path ledgerFilePath;

    public LedgerFileManager(String filePath) {
        this.ledgerFilePath = Paths.get(filePath);
        initLedgerFile();
    }

    /**
     * Initializes the ledger file and creates parent directories and CSV headers if they don't exist.
     */
    private void initLedgerFile() {
        try {
            if (ledgerFilePath.getParent() != null) {
                Files.createDirectories(ledgerFilePath.getParent());
            }
            if (!Files.exists(ledgerFilePath)) {
                Files.writeString(ledgerFilePath, CSV_HEADER + System.lineSeparator(), 
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                System.out.println("[FILE I/O] Initialized new persistent ledger file: " + ledgerFilePath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new BankingException("Failed to initialize ledger file: " + e.getMessage(), "FILE_INIT_ERROR", e);
        }
    }

    /**
     * Appends an immutable transaction record to disk.
     * Uses BufferedWriter with StandardOpenOption.APPEND.
     */
    public synchronized void appendTransaction(Transaction txn) {
        if (txn == null) return;

        String line = String.format("%s,%s,%s,%.2f,%.2f,\"%s\",%s%n",
                txn.getTransactionId(),
                txn.getAccountNumber(),
                txn.getType(),
                txn.getAmount(),
                txn.getBalanceAfter(),
                txn.getDescription().replace("\"", "\"\""), // Escape double quotes
                txn.getTimestamp().format(ISO_FORMATTER)
        );

        try (BufferedWriter writer = Files.newBufferedWriter(
                ledgerFilePath, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            writer.write(line);
            System.out.printf("[PERSISTENCE] Transaction %s appended to %s%n", 
                    txn.getTransactionId(), ledgerFilePath.getFileName());
        } catch (IOException e) {
            throw new BankingException("Failed to persist transaction to disk: " + e.getMessage(), "FILE_WRITE_ERROR", e);
        }
    }

    /**
     * Reads and parses all transactions from disk into memory.
     * Uses BufferedReader with try-with-resources.
     */
    public List<Transaction> loadAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        if (!Files.exists(ledgerFilePath)) {
            return transactions;
        }

        try (BufferedReader reader = Files.newBufferedReader(ledgerFilePath, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // Read header line
            int lineNum = 1;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                try {
                    String[] tokens = parseCsvLine(line);
                    if (tokens.length < 6) {
                        System.err.printf("[FILE I/O WARN] Skipping malformed line %d: %s%n", lineNum, line);
                        continue;
                    }

                    String txnId = tokens[0].trim();
                    String accNo = tokens[1].trim();
                    String type = tokens[2].trim();
                    double amount = Double.parseDouble(tokens[3].trim());
                    double balanceAfter = Double.parseDouble(tokens[4].trim());
                    String description = tokens[5].trim();

                    Transaction txn = new Transaction(txnId, accNo, type, amount, balanceAfter, description);
                    transactions.add(txn);
                } catch (Exception parseEx) {
                    System.err.printf("[FILE I/O ERROR] Error parsing line %d (%s): %s%n", lineNum, line, parseEx.getMessage());
                }
            }
        } catch (IOException e) {
            throw new BankingException("Failed to read transactions from disk: " + e.getMessage(), "FILE_READ_ERROR", e);
        }

        System.out.printf("[PERSISTENCE] Successfully loaded %d transactions from %s%n", 
                transactions.size(), ledgerFilePath.getFileName());
        return transactions;
    }

    /**
     * Helper to split CSV tokens while respecting quotes.
     */
    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }
}
