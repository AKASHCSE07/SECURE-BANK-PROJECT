package com.securebank.io;

import com.securebank.dsa.Transaction;
import com.securebank.prototype.Account;
import com.securebank.prototype.Customer;
import com.securebank.prototype.SavingsAccount;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * FileIOTestApp demonstrates Phase 5 File Handling & Persistence.
 */
public class FileIOTestApp {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("       SECUREBANK: PHASE 5 - FILE I/O & PERSISTENCE ENGINE        ");
        System.out.println("=================================================================\n");

        String ledgerPath = "data/ledger.csv";
        LedgerFileManager fileManager = new LedgerFileManager(ledgerPath);

        // 1. Create simulated customer and account
        Customer customer = new Customer("CUST-101", "Marcus", "Vance", "marcus.vance@securebank.com", "+1-800-555-9988");
        Account savings = new SavingsAccount("ACC-SAV-8001", customer.getCustomerId(), 2500.00, 0.045);
        customer.addAccount(savings);

        // 2. Persist Transactions to CSV disk file
        System.out.println("\n>>> 1. WRITING TRANSACTIONS TO DISK (APPEND-ONLY LEDGER) <<<");
        Transaction t1 = new Transaction("TXN-2026-001", savings.getAccountNumber(), "DEPOSIT", 1500.00, 4000.00, "Initial payroll direct deposit");
        Transaction t2 = new Transaction("TXN-2026-002", savings.getAccountNumber(), "WITHDRAWAL", 350.00, 3650.00, "ATM cash withdrawal");
        Transaction t3 = new Transaction("TXN-2026-003", savings.getAccountNumber(), "TRANSFER_SENT", 650.00, 3000.00, "Wire transfer to Landlord");
        Transaction t4 = new Transaction("TXN-2026-004", savings.getAccountNumber(), "DEPOSIT", 800.00, 3800.00, "Freelance invoice settlement");

        fileManager.appendTransaction(t1);
        fileManager.appendTransaction(t2);
        fileManager.appendTransaction(t3);
        fileManager.appendTransaction(t4);

        // 3. Read & Parse Transactions back from Disk
        System.out.println("\n>>> 2. READING & PARSING TRANSACTIONS FROM CSV <<<");
        List<Transaction> loadedTransactions = fileManager.loadAllTransactions();
        System.out.println("Transactions retrieved from persistent CSV:");
        for (Transaction txn : loadedTransactions) {
            System.out.println("   " + txn);
        }

        // 4. Export Formatted Monthly Bank Statement
        System.out.println("\n>>> 3. EXPORTING FORMAL ACCOUNT STATEMENT FILE <<<");
        Path statementPath = StatementExporter.exportAccountStatement(customer, savings, loadedTransactions);

        // 5. Read back and display the generated statement file
        System.out.println("\n>>> 4. PREVIEWING EXPORTED STATEMENT FILE CONTENT <<<");
        try {
            List<String> lines = Files.readAllLines(statementPath);
            lines.forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("Failed to read back statement: " + e.getMessage());
        }

        System.out.println("\n=================================================================");
        System.out.println("                PHASE 5 EXECUTION COMPLETE                       ");
        System.out.println("=================================================================");
    }
}
