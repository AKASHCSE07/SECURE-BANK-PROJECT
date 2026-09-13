package com.securebank.exception;

/**
 * Root unchecked exception for all SecureBank business logic errors.
 *
 * Why RuntimeException (Unchecked)?
 * 1. Financial transactions should roll back automatically when unchecked exceptions are thrown.
 * 2. Prevents cluttered "throws" clauses in every method signature.
 * 3. Can be caught and mapped to HTTP status codes by GlobalExceptionHandler in Phase 11.
 */
public class BankingException extends RuntimeException {
    private final String errorCode;

    public BankingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BankingException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
