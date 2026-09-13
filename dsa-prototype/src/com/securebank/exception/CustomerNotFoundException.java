package com.securebank.exception;

public class CustomerNotFoundException extends BankingException {
    private final String customerId;

    public CustomerNotFoundException(String customerId) {
        super("Customer not found with ID: " + customerId, "CUST_NOT_FOUND");
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }
}
