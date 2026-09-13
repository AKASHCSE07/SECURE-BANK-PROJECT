package com.securebank.prototype;

import java.util.ArrayList;
import java.util.List;

/**
 * Customer class representing a bank client.
 * Demonstrates:
 * 1. Encapsulation (private fields, validation in setters).
 * 2. Composition (A Customer "has-a" collection of Accounts).
 */
public class Customer {
    private final String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private final List<Account> accounts;

    public Customer(String customerId, String firstName, String lastName, String email, String phone) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be empty.");
        }
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.accounts = new ArrayList<>();
    }

    /**
     * Adds an account to the customer's portfolio.
     */
    public void addAccount(Account account) {
        if (account != null) {
            this.accounts.add(account);
            System.out.printf("[LINKED] Account %s successfully linked to customer %s %s.%n",
                    account.getAccountNumber(), firstName, lastName);
        }
    }

    /**
     * Finds an account owned by this customer by account number.
     */
    public Account getAccount(String accountNumber) {
        for (Account acc : accounts) {
            if (acc.getAccountNumber().equalsIgnoreCase(accountNumber)) {
                return acc;
            }
        }
        return null;
    }

    public void displayCustomerSummary() {
        System.out.println("==================================================");
        System.out.printf("CUSTOMER PROFILE: [%s] %s %s%n", customerId, firstName, lastName);
        System.out.printf("Email: %s | Phone: %s%n", email, phone);
        System.out.printf("Total Linked Accounts: %d%n", accounts.size());
        for (Account acc : accounts) {
            System.out.print("  -> ");
            acc.displayAccountInfo();
        }
        System.out.println("==================================================");
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(accounts); // Returns defensive copy
    }
}
