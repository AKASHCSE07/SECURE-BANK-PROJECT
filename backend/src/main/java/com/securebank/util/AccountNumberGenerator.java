package com.securebank.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Utility for generating unique, standardized bank account numbers and transaction references.
 */
public class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a 10-digit standardized account number: "SB" + 8 random digits.
     * Example: "SB-48201948"
     */
    public static String generateAccountNumber() {
        int random8Digits = 10000000 + RANDOM.nextInt(90000000);
        return "SB-" + random8Digits;
    }

    /**
     * Generates a unique transaction reference code.
     * Example: "TXN-A7F92B10"
     */
    public static String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
