package com.securebank.dsa;

import com.securebank.prototype.Account;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * AccountSortEngine demonstrates Sorting Algorithms & Custom Comparators.
 * In a banking system, administrators frequently generate reports ordered by:
 * - Highest Balance to Lowest Balance (High Net Worth audit)
 * - Account Number (Alphabetical/Numerical order)
 * - Transaction Date (Reverse Chronological for Statements)
 *
 * Demonstrates:
 * 1. Java Collections.sort() with custom lambdas / Comparators (TimSort: O(n log n)).
 * 2. Explicit MergeSort implementation to teach divide-and-conquer mechanics.
 */
public class AccountSortEngine {

    /**
     * Sorts accounts by Balance Descending (Highest balance first).
     * Time Complexity: O(n log n)
     */
    public static List<Account> sortByBalanceDescending(List<Account> accounts) {
        List<Account> sorted = new ArrayList<>(accounts);
        sorted.sort((a, b) -> Double.compare(b.getBalance(), a.getBalance()));
        return sorted;
    }

    /**
     * Sorts accounts by Account Number Ascending.
     * Time Complexity: O(n log n)
     */
    public static List<Account> sortByAccountNumberAscending(List<Account> accounts) {
        List<Account> sorted = new ArrayList<>(accounts);
        sorted.sort(Comparator.comparing(Account::getAccountNumber));
        return sorted;
    }

    /**
     * Explicit MergeSort Algorithm on Accounts by Balance.
     * Teaches the Divide-and-Conquer technique:
     * - Divide list into two halves: O(1)
     * - Recursively sort both halves: 2 * T(n/2)
     * - Merge the sorted halves: O(n)
     * Total Time Complexity: O(n log n), Space Complexity: O(n)
     */
    public static List<Account> mergeSortByBalance(List<Account> list) {
        if (list == null || list.size() <= 1) {
            return list;
        }

        int mid = list.size() / 2;
        List<Account> left = mergeSortByBalance(new ArrayList<>(list.subList(0, mid)));
        List<Account> right = mergeSortByBalance(new ArrayList<>(list.subList(mid, list.size())));

        return merge(left, right);
    }

    private static List<Account> merge(List<Account> left, List<Account> right) {
        List<Account> merged = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            if (left.get(i).getBalance() >= right.get(j).getBalance()) {
                merged.add(left.get(i));
                i++;
            } else {
                merged.add(right.get(j));
                j++;
            }
        }

        while (i < left.size()) merged.add(left.get(i++));
        while (j < right.size()) merged.add(right.get(j++));

        return merged;
    }
}
