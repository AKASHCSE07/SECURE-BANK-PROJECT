package com.securebank.io;

import com.securebank.dsa.Transaction;
import com.securebank.exception.BankingException;
import com.securebank.prototype.Account;
import com.securebank.prototype.Customer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * StatementExporter generates official monthly customer account statements
 * and writes them to persistent text files.
 */
public class StatementExporter {

    private static final String STATEMENT_DIR = "data/statements";

    /**
     * Exports a formatted text statement for a specific account.
     */
    public static Path exportAccountStatement(Customer customer, Account account, List<Transaction> transactions) {
        if (customer == null || account == null || transactions == null) {
            throw new BankingException("Cannot export statement: Missing customer or account data.", "STATEMENT_DATA_NULL");
        }

        try {
            Path dirPath = Paths.get(STATEMENT_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String fileName = String.format("Statement_%s_%s.txt", 
                    account.getAccountNumber(), 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            Path filePath = dirPath.resolve(fileName);

            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write("========================================================================\n");
                writer.write("                      SECUREBANK ACCOUNT STATEMENT                      \n");
                writer.write("========================================================================\n\n");
                writer.write(String.format("Date Generated: %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                writer.write(String.format("Customer Name : %s\n", customer.getFullName()));
                writer.write(String.format("Customer ID   : %s\n", customer.getCustomerId()));
                writer.write(String.format("Email         : %s\n", customer.getEmail()));
                writer.write(String.format("Account Number: %s\n", account.getAccountNumber()));
                writer.write(String.format("Account Type  : %s\n", account.getClass().getSimpleName()));
                writer.write(String.format("Current Status: %s\n", account.getStatus()));
                writer.write(String.format("Current Balance: $%.2f\n\n", account.getBalance()));
                writer.write("------------------------------------------------------------------------\n");
                writer.write(String.format("%-14s | %-16s | %-10s | %-10s | %s\n", 
                        "TXN ID", "TYPE", "AMOUNT", "BALANCE", "DESCRIPTION"));
                writer.write("------------------------------------------------------------------------\n");

                double totalDeposited = 0;
                double totalWithdrawn = 0;

                for (Transaction txn : transactions) {
                    if (txn.getAccountNumber().equalsIgnoreCase(account.getAccountNumber())) {
                        writer.write(String.format("%-14s | %-16s | $%9.2f | $%9.2f | %s\n",
                                txn.getTransactionId(),
                                txn.getType(),
                                txn.getAmount(),
                                txn.getBalanceAfter(),
                                txn.getDescription()
                        ));

                        if ("DEPOSIT".equalsIgnoreCase(txn.getType()) || "TRANSFER_RECEIVED".equalsIgnoreCase(txn.getType())) {
                            totalDeposited += txn.getAmount();
                        } else {
                            totalWithdrawn += txn.getAmount();
                        }
                    }
                }

                writer.write("------------------------------------------------------------------------\n");
                writer.write(String.format("Total Inflow  (Deposits/Credits) : $%.2f\n", totalDeposited));
                writer.write(String.format("Total Outflow (Debits/Transfers) : $%.2f\n", totalWithdrawn));
                writer.write(String.format("Net Financial Movement           : $%.2f\n", (totalDeposited - totalWithdrawn)));
                writer.write("========================================================================\n");
                writer.write("          Thank you for banking with SecureBank International           \n");
                writer.write("========================================================================\n");
            }

            System.out.printf("[STATEMENT GENERATED] Successfully written to: %s%n", filePath.toAbsolutePath());
            return filePath;

        } catch (IOException e) {
            throw new BankingException("Failed to write bank statement file: " + e.getMessage(), "STATEMENT_IO_ERROR", e);
        }
    }
}
