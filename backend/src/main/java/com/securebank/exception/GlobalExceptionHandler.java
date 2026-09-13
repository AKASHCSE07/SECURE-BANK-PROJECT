package com.securebank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception interceptor for all REST Controllers.
 * Translates domain exceptions into HTTP status codes and structured ErrorDetails JSON.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Jakarta Bean Validation errors (@Valid triggers).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorDetails errorDetails = new ErrorDetails("Validation failed for one or more fields.", "VALIDATION_ERROR", errors);
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AccountNotFoundException.class, CustomerNotFoundException.class})
    public ResponseEntity<ErrorDetails> handleNotFoundExceptions(BankingException ex, WebRequest request) {
        ErrorDetails error = new ErrorDetails(ex.getMessage(), ex.getErrorCode(), request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorDetails> handleInsufficientBalance(InsufficientBalanceException ex, WebRequest request) {
        ErrorDetails error = new ErrorDetails(ex.getMessage(), ex.getErrorCode(), request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY); // 422
    }

    @ExceptionHandler(AccountBlockedException.class)
    public ResponseEntity<ErrorDetails> handleAccountBlocked(AccountBlockedException ex, WebRequest request) {
        ErrorDetails error = new ErrorDetails(ex.getMessage(), ex.getErrorCode(), request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN); // 403
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ErrorDetails> handleUnauthorized(UnauthorizedOperationException ex, WebRequest request) {
        ErrorDetails error = new ErrorDetails(ex.getMessage(), ex.getErrorCode(), request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED); // 401
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorDetails> handleDuplicateResource(DuplicateResourceException ex, WebRequest request) {
        ErrorDetails error = new ErrorDetails(ex.getMessage(), ex.getErrorCode(), request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.CONFLICT); // 409
    }

    @ExceptionHandler(BankingException.class)
    public ResponseEntity<ErrorDetails> handleGenericBankingException(BankingException ex, WebRequest request) {
        ErrorDetails error = new ErrorDetails(ex.getMessage(), ex.getErrorCode(), request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDetails error = new ErrorDetails("An unexpected internal server error occurred.", "INTERNAL_SERVER_ERROR", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
