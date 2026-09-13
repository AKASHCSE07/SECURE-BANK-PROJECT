package com.securebank.exception;

public class DuplicateResourceException extends BankingException {
    public DuplicateResourceException(String resourceName, String field) {
        super(String.format("%s already exists with %s", resourceName, field), "DUPLICATE_RESOURCE");
    }

    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE");
    }
}
