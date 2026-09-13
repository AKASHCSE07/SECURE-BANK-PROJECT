package com.securebank.exception;

public class CustomerNotFoundException extends BankingException {
    public CustomerNotFoundException(String identifier) {
        super("Customer not found with identifier: " + identifier, "CUSTOMER_NOT_FOUND");
    }
}
