package com.securebank.prototype;

/**
 * CurrentAccount extends Account.
 * Demonstrates:
 * 1. Inheritance (inherits common account properties).
 * 2. Polymorphism (overrides abstract withdraw() method with overdraft protection logic).
 */
public class CurrentAccount extends Account {
    private double overdraftLimit; // Maximum negative balance allowed (e.g., $1000.00)

    public CurrentAccount(String accountNumber, String customerId, double initialBalance, double overdraftLimit) {
        super(accountNumber, customerId, initialBalance);
        if (overdraftLimit < 0) {
            throw new IllegalArgumentException("Overdraft limit cannot be negative.");
        }
        this.overdraftLimit = overdraftLimit;
    }

    /**
     * Polymorphic implementation of withdraw.
     * Enforces the business rule: Total available funds = balance + overdraftLimit.
     */
    @Override
    public boolean withdraw(double amount) {
        if (!"ACTIVE".equalsIgnoreCase(getStatus())) {
            System.out.println("[FAILED] Withdrawal denied: Account " + getAccountNumber() + " is " + getStatus() + ".");
            return false;
        }
        if (amount <= 0) {
            System.out.println("[FAILED] Withdrawal amount must be greater than 0.");
            return false;
        }

        double availableFunds = this.balance + this.overdraftLimit;
        if (amount > availableFunds) {
            System.out.printf("[FAILED] Overdraft limit exceeded for Current Account %s. Available (Balance + Overdraft): $%.2f, Requested: $%.2f%n",
                    getAccountNumber(), availableFunds, amount);
            return false;
        }

        this.balance -= amount;
        System.out.printf("[SUCCESS] Withdrew $%.2f from Current Account %s. New Balance: $%.2f (Overdraft Limit: $%.2f)%n",
                amount, getAccountNumber(), this.balance, this.overdraftLimit);
        return true;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }
}
