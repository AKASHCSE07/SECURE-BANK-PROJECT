package com.securebank.exception;

public class InvalidAmountException extends BankingException {
    private final double invalidAmount;

    public InvalidAmountException(double invalidAmount, String message) {
        super(String.format("Invalid financial amount: $%.2f. %s", invalidAmount, message), "INVALID_AMOUNT");
        this.invalidAmount = invalidAmount;
    }

    public double getInvalidAmount() {
        return invalidAmount;
    }
}
