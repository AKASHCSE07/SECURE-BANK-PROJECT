package com.securebank.exception;

public class InsufficientBalanceException extends BankingException {
    private final String accountNumber;
    private final double requestedAmount;
    private final double availableBalance;

    public InsufficientBalanceException(String accountNumber, double requestedAmount, double availableBalance) {
        super(String.format("Insufficient funds in account %s. Requested: $%.2f, Available: $%.2f",
                accountNumber, requestedAmount, availableBalance), "INSUFFICIENT_FUNDS");
        this.accountNumber = accountNumber;
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }

    public String getAccountNumber() { return accountNumber; }
    public double getRequestedAmount() { return requestedAmount; }
    public double getAvailableBalance() { return availableBalance; }
}
