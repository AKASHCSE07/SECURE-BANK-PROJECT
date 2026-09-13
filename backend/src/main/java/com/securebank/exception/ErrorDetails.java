package com.securebank.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized JSON structure returned whenever an exception is caught by GlobalExceptionHandler.
 */
public class ErrorDetails {
    private boolean success = false;
    private String message;
    private String errorCode;
    private String details;
    private Map<String, String> validationErrors;
    private LocalDateTime timestamp;

    public ErrorDetails() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorDetails(String message, String errorCode, String details) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorDetails(String message, String errorCode, Map<String, String> validationErrors) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.validationErrors = validationErrors;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public Map<String, String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(Map<String, String> validationErrors) { this.validationErrors = validationErrors; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
