package com.securebank.exception;

public class DuplicateResourceException extends BankingException {
    private final String resourceName;
    private final String identifier;

    public DuplicateResourceException(String resourceName, String identifier) {
        super(String.format("Duplicate resource detected: %s with identifier '%s' already exists.",
                resourceName, identifier), "DUPLICATE_RESOURCE");
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public String getResourceName() { return resourceName; }
    public String getIdentifier() { return identifier; }
}
