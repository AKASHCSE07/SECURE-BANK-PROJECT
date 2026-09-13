package com.securebank.exception;

public class AccountBlockedException extends BankingException {
    private final String accountNumber;
    private final String currentStatus;

    public AccountBlockedException(String accountNumber, String currentStatus) {
        super(String.format("Operation rejected: Account %s is currently %s.", accountNumber, currentStatus), "ACC_BLOCKED");
        this.accountNumber = accountNumber;
        this.currentStatus = currentStatus;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getCurrentStatus() { return currentStatus; }
}
