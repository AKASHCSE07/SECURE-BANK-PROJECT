package com.securebank.exception;

public class AccountBlockedException extends BankingException {
    public AccountBlockedException(String accountNumber, String status) {
        super(String.format("Account %s is %s and cannot perform this transaction.", accountNumber, status), "ACCOUNT_BLOCKED");
    }

    public AccountBlockedException(String message) {
        super(message, "ACCOUNT_BLOCKED");
    }
}
