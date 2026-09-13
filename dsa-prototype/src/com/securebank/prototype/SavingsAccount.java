package com.securebank.prototype;

/**
 * SavingsAccount extends Account.
 * Demonstrates:
 * 1. Inheritance (extends base Account class).
 * 2. Polymorphism (overrides abstract withdraw() method with minimum balance invariant).
 */
public class SavingsAccount extends Account {
    private static final double MINIMUM_BALANCE = 500.00;
    private double interestRate; // e.g., 0.035 for 3.5%

    public SavingsAccount(String accountNumber, String customerId, double initialBalance, double interestRate) {
        super(accountNumber, customerId, initialBalance);
        if (initialBalance < MINIMUM_BALANCE) {
            throw new IllegalArgumentException(
                "Initial balance for SavingsAccount cannot be less than minimum balance: $" + MINIMUM_BALANCE
            );
        }
        this.interestRate = interestRate;
    }

    /**
     * Polymorphic implementation of withdraw.
     * Enforces the business rule: Balance must never fall below MINIMUM_BALANCE ($500.00).
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
        if (this.balance - amount < MINIMUM_BALANCE) {
            System.out.printf("[FAILED] Insufficient funds in Savings Account %s. Minimum balance required is $%.2f. Current Balance: $%.2f, Requested: $%.2f%n",
                    getAccountNumber(), MINIMUM_BALANCE, this.balance, amount);
            return false;
        }

        this.balance -= amount;
        System.out.printf("[SUCCESS] Withdrew $%.2f from Savings Account %s. Remaining Balance: $%.2f%n",
                amount, getAccountNumber(), this.balance);
        return true;
    }

    /**
     * Applies monthly accrued interest to the savings balance.
     */
    public void applyInterest() {
        if ("ACTIVE".equalsIgnoreCase(getStatus())) {
            double interestAmount = this.balance * (this.interestRate / 12.0);
            this.balance += interestAmount;
            System.out.printf("[INTEREST APPLIED] Added $%.2f interest to Account %s. New Balance: $%.2f%n",
                    interestAmount, getAccountNumber(), this.balance);
        }
    }

    public double getInterestRate() {
        return interestRate;
    }

    public static double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }
}
