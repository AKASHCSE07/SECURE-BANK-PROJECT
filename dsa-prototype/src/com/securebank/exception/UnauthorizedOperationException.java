package com.securebank.exception;

public class UnauthorizedOperationException extends BankingException {
    public UnauthorizedOperationException(String message) {
        super(message, "UNAUTHORIZED_OP");
    }
}
