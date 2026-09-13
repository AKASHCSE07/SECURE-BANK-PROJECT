package com.securebank.dsa;

import com.securebank.prototype.Account;
import com.securebank.prototype.CurrentAccount;
import com.securebank.prototype.SavingsAccount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * DSAMain: Comprehensive demonstration of Data Structures & Algorithms
 * applied to the SecureBank domain.
 */
public class DSAMain {
    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("          SECUREBANK: PHASE 3 - DATA STRUCTURES & DSA ENGINE      ");
        System.out.println("=================================================================\n");

        BankDSARegistry registry = new BankDSARegistry();

        // -------------------------------------------------------------
        // SECTION 1: HASHMAP & HASHSET (Fast Lookups & Unique Accounts)
        // -------------------------------------------------------------
        System.out.println(">>> 1. DEMONSTRATING HASHMAP (O(1) LOOKUP) & HASHSET (O(1) DEDUP) <<<");
        Account a1 = new SavingsAccount("ACC-101", "CUST-1", 4500.00, 0.035);
        Account a2 = new CurrentAccount("ACC-102", "CUST-2", 1200.00, 1000.00);
        Account a3 = new SavingsAccount("ACC-103", "CUST-3", 8900.00, 0.040);
        Account a4 = new CurrentAccount("ACC-104", "CUST-4", 250.00, 300.00);

        registry.registerAccount(a1);
        registry.registerAccount(a2);
        registry.registerAccount(a3);
        registry.registerAccount(a4);

        System.out.println("\nAttempting to re-register duplicate account 'ACC-101'...");
        Account duplicate = new SavingsAccount("ACC-101", "CUST-99", 500.00, 0.02);
        registry.registerAccount(duplicate); // HashSet rejects in O(1)

        System.out.println("\nQuerying HashMap for 'ACC-103' in O(1) time:");
        Account found = registry.findAccount("ACC-103");
        if (found != null) {
            System.out.print("Found: ");
            found.displayAccountInfo();
        }

        // -------------------------------------------------------------
        // SECTION 2: ARRAYLIST & STACK (Transaction Ledger & LIFO Undo)
        // -------------------------------------------------------------
        System.out.println("\n>>> 2. DEMONSTRATING ARRAYLIST (LEDGER) & STACK (ACTION HISTORY) <<<");
        Transaction t1 = new Transaction("TXN-1001", "ACC-101", "DEPOSIT", 1000.00, 5500.00, "Salary credit");
        Transaction t2 = new Transaction("TXN-1002", "ACC-102", "WITHDRAWAL", 200.00, 1000.00, "ATM cash");
        Transaction t3 = new Transaction("TXN-1003", "ACC-101", "TRANSFER_SENT", 500.00, 5000.00, "Rent to Bob");
        Transaction t4 = new Transaction("TXN-1004", "ACC-103", "DEPOSIT", 3000.00, 11900.00, "Investment dividend");

        registry.recordTransaction(t1);
        registry.recordTransaction(t2);
        registry.recordTransaction(t3);
        registry.recordTransaction(t4);

        System.out.println("Popping recent user actions from LIFO Stack:");
        System.out.println("  Last Action: " + registry.undoOrPopLastAction());
        System.out.println("  Prior Action: " + registry.undoOrPopLastAction());

        // -------------------------------------------------------------
        // SECTION 3: QUEUE (FIFO Customer Service Desk)
        // -------------------------------------------------------------
        System.out.println("\n>>> 3. DEMONSTRATING QUEUE (FIFO GENERAL SUPPORT QUEUE) <<<");
        CustomerServiceQueue standardQueue = new CustomerServiceQueue();
        standardQueue.submitTicket("TCK-001", "CUST-1", "Forgot online banking username");
        standardQueue.submitTicket("TCK-002", "CUST-2", "Request paper account statement");
        standardQueue.submitTicket("TCK-003", "CUST-3", "Inquire about auto loan interest rates");

        System.out.println("\nProcessing customer tickets in FIFO arrival order:");
        standardQueue.processNextTicket();
        standardQueue.processNextTicket();

        // -------------------------------------------------------------
        // SECTION 4: PRIORITY QUEUE (Heap - VIP & Critical Fraud Desk)
        // -------------------------------------------------------------
        System.out.println("\n>>> 4. DEMONSTRATING PRIORITYQUEUE (MIN-HEAP VIP & INCIDENT QUEUE) <<<");
        PrioritySupportQueue vipQueue = new PrioritySupportQueue();

        // Customer A: Standard ticket with $500 balance
        vipQueue.enqueue(new PrioritySupportQueue.SupportRequest(
                "VIP-001", "General Dave", PrioritySupportQueue.PriorityTier.STANDARD, 500.00, "Address change"));

        // Customer B: High Priority with $20,000 balance
        vipQueue.enqueue(new PrioritySupportQueue.SupportRequest(
                "VIP-002", "Corporate Emma", PrioritySupportQueue.PriorityTier.HIGH_PRIORITY, 20000.00, "Wire delay"));

        // Customer C: Critical Fraud (Urgent Card Block!) with $1,200 balance -> Should jump to #1!
        vipQueue.enqueue(new PrioritySupportQueue.SupportRequest(
                "VIP-003", "Victim John", PrioritySupportQueue.PriorityTier.CRITICAL_FRAUD, 1200.00, "Stolen Debit Card!"));

        // Customer D: High Priority with $85,000 balance -> Should jump ahead of Emma (higher balance tie-breaker)!
        vipQueue.enqueue(new PrioritySupportQueue.SupportRequest(
                "VIP-004", "Investor Sarah", PrioritySupportQueue.PriorityTier.HIGH_PRIORITY, 85000.00, "Wire delay"));

        System.out.println("\nDispatching priority tickets from Heap (Critical & High-Balance first):");
        while (vipQueue.size() > 0) {
            vipQueue.serveNextPriorityCustomer();
        }

        // -------------------------------------------------------------
        // SECTION 5: SEARCHING ALGORITHMS (Linear vs. Binary Search)
        // -------------------------------------------------------------
        System.out.println("\n>>> 5. DEMONSTRATING SEARCHING ALGORITHMS <<<");
        List<Transaction> ledger = registry.getAllTransactions();

        System.out.println("A. Linear Search for transactions with keyword 'Rent':");
        List<Transaction> rentTxns = TransactionSearchEngine.linearSearchByKeyword(ledger, "Rent");
        rentTxns.forEach(t -> System.out.println("   -> " + t));

        System.out.println("\nB. Binary Search on sorted transaction list:");
        // Ensure sorted by transactionId
        List<Transaction> sortedById = new ArrayList<>(ledger);
        sortedById.sort(Comparator.comparing(Transaction::getTransactionId));

        System.out.println("Searching for target ID 'TXN-1003' using Binary Search:");
        Transaction foundTxn = TransactionSearchEngine.binarySearchById(sortedById, "TXN-1003");
        if (foundTxn != null) {
            System.out.println("   -> Result: " + foundTxn);
        }

        // -------------------------------------------------------------
        // SECTION 6: SORTING ALGORITHMS (MergeSort / Comparators)
        // -------------------------------------------------------------
        System.out.println("\n>>> 6. DEMONSTRATING SORTING ALGORITHMS (MERGESORT BY BALANCE) <<<");
        List<Account> allAccounts = registry.getAllAccounts();

        System.out.println("Original Account Order:");
        allAccounts.forEach(a -> System.out.printf("   Account: %s | Balance: $%.2f%n", a.getAccountNumber(), a.getBalance()));

        System.out.println("\nAccounts Sorted by Balance Descending (MergeSort O(n log n)):");
        List<Account> sortedByBalance = AccountSortEngine.mergeSortByBalance(allAccounts);
        sortedByBalance.forEach(a -> System.out.printf("   Account: %s | Balance: $%.2f%n", a.getAccountNumber(), a.getBalance()));

        System.out.println("\n=================================================================");
        System.out.println("                PHASE 3 EXECUTION COMPLETE                       ");
        System.out.println("=================================================================");
    }
}
