package com.securebank.exception;

public class InsufficientBalanceException extends BankingException {
    public InsufficientBalanceException(String accountNumber, double requestedAmount, double availableBalance) {
        super(String.format("Insufficient funds in account %s: requested amount is %.2f but only %.2f is available.", 
                accountNumber, requestedAmount, availableBalance), "INSUFFICIENT_BALANCE");
    }

    public InsufficientBalanceException(String message) {
        super(message, "INSUFFICIENT_BALANCE");
    }
}
