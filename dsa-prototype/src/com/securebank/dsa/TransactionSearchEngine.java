package com.securebank.dsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TransactionSearchEngine demonstrates Searching Algorithms:
 * 1. Linear Search: Filter transactions by keyword or amount range - Time Complexity: O(n).
 * 2. Binary Search: Fast lookup by Transaction ID in a sorted list - Time Complexity: O(log n).
 */
public class TransactionSearchEngine {

    /**
     * Linear Search: Filters transactions containing a search keyword in description or type.
     * Time Complexity: O(n) where n is the number of transactions.
     * Space Complexity: O(k) where k is the number of matching results.
     */
    public static List<Transaction> linearSearchByKeyword(List<Transaction> transactions, String keyword) {
        List<Transaction> results = new ArrayList<>();
        if (transactions == null || keyword == null) return results;

        String lowerKey = keyword.toLowerCase();
        for (Transaction txn : transactions) {
            if (txn.getDescription().toLowerCase().contains(lowerKey) || 
                txn.getType().toLowerCase().contains(lowerKey) ||
                txn.getAccountNumber().toLowerCase().contains(lowerKey)) {
                results.add(txn);
            }
        }
        return results;
    }

    /**
     * Linear Search: Filter transactions within a price/amount range.
     * Time Complexity: O(n).
     */
    public static List<Transaction> filterByAmountRange(List<Transaction> transactions, double min, double max) {
        List<Transaction> results = new ArrayList<>();
        for (Transaction txn : transactions) {
            if (txn.getAmount() >= min && txn.getAmount() <= max) {
                results.add(txn);
            }
        }
        return results;
    }

    /**
     * Binary Search: Searches for a transaction by transactionId in a sorted list.
     *
     * Precondition: The list MUST be sorted by transactionId beforehand.
     * Time Complexity: O(log n).
     *
     * @param sortedTransactions List sorted ascending by transactionId
     * @param targetId           The ID being searched
     * @return The Transaction if found, or null otherwise
     */
    public static Transaction binarySearchById(List<Transaction> sortedTransactions, String targetId) {
        if (sortedTransactions == null || targetId == null || sortedTransactions.isEmpty()) {
            return null;
        }

        int low = 0;
        int high = sortedTransactions.size() - 1;
        int comparisons = 0;

        while (low <= high) {
            comparisons++;
            int mid = low + (high - low) / 2; // Prevents integer overflow
            Transaction midTxn = sortedTransactions.get(mid);
            int cmp = midTxn.getTransactionId().compareTo(targetId);

            if (cmp == 0) {
                System.out.printf("[BINARY SEARCH] Found ID '%s' at index %d in %d comparisons.%n",
                        targetId, mid, comparisons);
                return midTxn;
            } else if (cmp < 0) {
                low = mid + 1; // Search right half
            } else {
                high = mid - 1; // Search left half
            }
        }

        System.out.printf("[BINARY SEARCH] ID '%s' not found after %d comparisons.%n", targetId, comparisons);
        return null;
    }
}
