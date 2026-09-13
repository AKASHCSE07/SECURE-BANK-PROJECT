package com.securebank.exception;

public class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(String accountNumber) {
        super("Account not found with account number: " + accountNumber, "ACCOUNT_NOT_FOUND");
    }
}
