package com.securebank.validator;

import com.securebank.exception.BankingException;
import com.securebank.exception.InvalidAmountException;
import com.securebank.exception.UnauthorizedOperationException;

import java.util.regex.Pattern;

/**
 * BankingValidator provides central validation rules for inputs, credentials, and financial amounts.
 * Implements "Fail-Fast" architecture: Validate upfront before touching domain models or databases.
 */
public class BankingValidator {

    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private static final Pattern ACCOUNT_NO_PATTERN = 
            Pattern.compile("^[A-Za-z0-9-]{6,20}$");

    private static final Pattern PHONE_PATTERN = 
            Pattern.compile("^[+0-9- ]{10,15}$");

    /**
     * Validates that a financial transaction amount is strictly positive and finite.
     */
    public static void validatePositiveAmount(double amount, String operation) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new InvalidAmountException(amount, "Amount is not a valid finite number.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException(amount, operation + " amount must be strictly greater than $0.00.");
        }
    }

    /**
     * Validates account number format.
     */
    public static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || !ACCOUNT_NO_PATTERN.matcher(accountNumber.trim()).matches()) {
            throw new BankingException(
                "Invalid account number format. Must be 6-20 alphanumeric characters.", "INVALID_ACC_FORMAT");
        }
    }

    /**
     * Validates email format.
     */
    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new BankingException("Invalid email address: " + email, "INVALID_EMAIL");
        }
    }

    /**
     * Validates phone format.
     */
    public static void validatePhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new BankingException("Invalid phone number format: " + phone, "INVALID_PHONE");
        }
    }

    /**
     * Validates transfer preconditions.
     */
    public static void validateTransferAccounts(String sourceAcc, String targetAcc) {
        validateAccountNumber(sourceAcc);
        validateAccountNumber(targetAcc);
        if (sourceAcc.equalsIgnoreCase(targetAcc)) {
            throw new UnauthorizedOperationException("Cannot transfer funds to the same source account: " + sourceAcc);
        }
    }
}
