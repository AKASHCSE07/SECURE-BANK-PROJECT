package com.securebank.prototype;

/**
 * Abstract base class representing a generic Bank Account.
 * Demonstrates:
 * 1. Encapsulation (private fields, protected balance, public controlled methods).
 * 2. Abstraction (cannot be instantiated directly; defines abstract withdraw logic).
 */
public abstract class Account {
    private final String accountNumber;
    private final String customerId;
    protected double balance;
    private String status; // "ACTIVE", "BLOCKED", "CLOSED"

    /**
     * Parameterized Constructor
     */
    public Account(String accountNumber, String customerId, double initialBalance) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty.");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.balance = initialBalance;
        this.status = "ACTIVE";
    }

    /**
     * Deposits money into the account.
     * Guaranteed invariant: Balance increases only if amount is strictly positive and account is ACTIVE.
     *
     * @param amount Amount to deposit
     * @return true if successful, false otherwise
     */
    public boolean deposit(double amount) {
        if (!"ACTIVE".equalsIgnoreCase(this.status)) {
            System.out.println("[FAILED] Cannot deposit: Account " + accountNumber + " is " + status + ".");
            return false;
        }
        if (amount <= 0) {
            System.out.println("[FAILED] Cannot deposit: Amount must be greater than 0.");
            return false;
        }
        this.balance += amount;
        System.out.printf("[SUCCESS] Deposited $%.2f into Account %s. New Balance: $%.2f%n", 
                amount, accountNumber, this.balance);
        return true;
    }

    /**
     * Abstract withdrawal method to be implemented by concrete subclasses.
     * Demonstrates Polymorphism: SavingsAccount and CurrentAccount enforce distinct financial policies.
     *
     * @param amount Amount to withdraw
     * @return true if withdrawal succeeded, false otherwise
     */
    public abstract boolean withdraw(double amount);

    /**
     * Transfers money from this account to a target account.
     * Demonstrates Object Interaction & Behavioral Coordination.
     *
     * @param targetAccount Destination account
     * @param amount        Amount to transfer
     * @return true if transfer completed, false otherwise
     */
    public boolean transfer(Account targetAccount, double amount) {
        if (targetAccount == null) {
            System.out.println("[FAILED] Target account does not exist.");
            return false;
        }
        if (this.accountNumber.equals(targetAccount.getAccountNumber())) {
            System.out.println("[FAILED] Source and destination accounts cannot be identical.");
            return false;
        }
        if (amount <= 0) {
            System.out.println("[FAILED] Transfer amount must be positive.");
            return false;
        }

        System.out.printf("--> Initiating transfer of $%.2f from %s to %s...%n", 
                amount, this.accountNumber, targetAccount.getAccountNumber());

        // Step 1: Withdraw from source account (enforces subclass polymorphic rules)
        boolean withdrawSuccess = this.withdraw(amount);
        if (!withdrawSuccess) {
            System.out.println("[FAILED] Transfer aborted: Source withdrawal failed.");
            return false;
        }

        // Step 2: Deposit into target account
        boolean depositSuccess = targetAccount.deposit(amount);
        if (!depositSuccess) {
            // Rollback the withdrawal if deposit fails
            System.out.println("[ROLLBACK] Deposit to target failed! Refunding source account...");
            this.balance += amount;
            return false;
        }

        System.out.printf("[SUCCESS] Transfer of $%.2f completed successfully.%n", amount);
        return true;
    }

    // Getters and status modifiers
    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getBalance() {
        return balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void displayAccountInfo() {
        System.out.printf("Account: %s | Customer ID: %s | Type: %s | Status: %s | Balance: $%.2f%n",
                accountNumber, customerId, this.getClass().getSimpleName(), status, balance);
    }
}
