package com.securebank.dsa;

import com.securebank.prototype.Account;
import java.util.*;

/**
 * BankDSARegistry demonstrates in-memory data structures:
 * 1. HashMap<String, Account>: O(1) instantaneous account lookup by Account Number.
 * 2. HashSet<String>: O(1) uniqueness check preventing duplicate account numbers or duplicate beneficiaries.
 * 3. ArrayList<Transaction>: O(1) append for fast chronological transaction ledger recording.
 * 4. Stack<String>: LIFO tracking of recent user navigation/actions for undo/history tracking.
 */
public class BankDSARegistry {
    // 1. HashMap: Key = accountNumber, Value = Account object (O(1) lookup)
    private final Map<String, Account> accountMap;

    // 2. HashSet: Tracks allocated account numbers to guarantee uniqueness (O(1) search)
    private final Set<String> registeredAccountNumbers;

    // 3. ArrayList: Chronological transaction store
    private final List<Transaction> transactionLedger;

    // 4. Stack: LIFO action history (User action breadcrumbs)
    private final Deque<String> actionHistoryStack;

    public BankDSARegistry() {
        this.accountMap = new HashMap<>();
        this.registeredAccountNumbers = new HashSet<>();
        this.transactionLedger = new ArrayList<>();
        this.actionHistoryStack = new ArrayDeque<>();
    }

    /**
     * Registers an account into the system.
     * Demonstrates HashSet for uniqueness and HashMap for fast storage.
     */
    public boolean registerAccount(Account account) {
        if (account == null) return false;
        String accNo = account.getAccountNumber();

        // HashSet check: O(1) average time complexity
        if (registeredAccountNumbers.contains(accNo)) {
            System.out.println("[DSA ERROR] Duplicate Account Number detected via HashSet: " + accNo);
            return false;
        }

        registeredAccountNumbers.add(accNo);
        accountMap.put(accNo, account);
        recordAction("Registered account " + accNo);
        System.out.printf("[DSA REGISTRY] Account %s registered into HashMap & HashSet (Total accounts: %d)%n",
                accNo, accountMap.size());
        return true;
    }

    /**
     * Finds an account in O(1) average time using HashMap.
     */
    public Account findAccount(String accountNumber) {
        return accountMap.get(accountNumber);
    }

    /**
     * Appends a transaction to the ArrayList ledger in O(1) amortized time.
     */
    public void recordTransaction(Transaction transaction) {
        this.transactionLedger.add(transaction);
        recordAction("Logged transaction " + transaction.getTransactionId());
    }

    /**
     * Records a user action on the LIFO Stack.
     */
    public void recordAction(String action) {
        this.actionHistoryStack.push(action);
    }

    /**
     * Pops and returns the most recent action (LIFO).
     */
    public String undoOrPopLastAction() {
        if (actionHistoryStack.isEmpty()) {
            return "No actions to pop.";
        }
        return actionHistoryStack.pop();
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactionLedger); // Defensive copy
    }

    public List<Account> getAllAccounts() {
        return new ArrayList<>(accountMap.values());
    }

    public int getTotalAccountsCount() {
        return accountMap.size();
    }
}
