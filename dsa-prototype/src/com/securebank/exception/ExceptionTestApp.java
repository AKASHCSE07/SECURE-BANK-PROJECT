package com.securebank.exception;

import com.securebank.prototype.Account;
import com.securebank.prototype.SavingsAccount;
import com.securebank.validator.BankingValidator;

/**
 * ExceptionTestApp demonstrates:
 * 1. Throwing domain-specific custom exceptions.
 * 2. Try-Catch-Finally exception handling patterns.
 * 3. Fail-fast input validation.
 * 4. Error code and diagnostic metadata extraction.
 */
public class ExceptionTestApp {

    public static void executeWithdrawal(Account account, double amount) {
        System.out.printf("%n[OPERATION] Initiating withdrawal of $%.2f from account %s...%n", 
                amount, account != null ? account.getAccountNumber() : "NULL");

        try {
            // 1. Fail-fast Input Validation
            if (account == null) {
                throw new AccountNotFoundException("NULL_REFERENCE");
            }
            BankingValidator.validatePositiveAmount(amount, "Withdrawal");

            // 2. Account Status Check
            if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
                throw new AccountBlockedException(account.getAccountNumber(), account.getStatus());
            }

            // 3. Balance Check
            double currentBalance = account.getBalance();
            double minBalance = SavingsAccount.getMinimumBalance();
            if (currentBalance - amount < minBalance) {
                throw new InsufficientBalanceException(account.getAccountNumber(), amount, currentBalance - minBalance);
            }

            // 4. Perform Withdrawal
            account.withdraw(amount);
            System.out.printf("[SUCCESS] Withdrawal processed. New balance: $%.2f%n", account.getBalance());

        } catch (InvalidAmountException ex) {
            System.err.printf("[ERROR CAUGHT] ErrorCode: %s | Invalid Amount: $%.2f | Msg: %s%n",
                    ex.getErrorCode(), ex.getInvalidAmount(), ex.getMessage());
        } catch (AccountBlockedException ex) {
            System.err.printf("[ERROR CAUGHT] ErrorCode: %s | Account: %s | Status: %s | Msg: %s%n",
                    ex.getErrorCode(), ex.getAccountNumber(), ex.getCurrentStatus(), ex.getMessage());
        } catch (InsufficientBalanceException ex) {
            System.err.printf("[ERROR CAUGHT] ErrorCode: %s | Account: %s | Requested: $%.2f | Available: $%.2f%n",
                    ex.getErrorCode(), ex.getAccountNumber(), ex.getRequestedAmount(), ex.getAvailableBalance());
        } catch (BankingException ex) {
            System.err.printf("[GENERIC BANKING ERROR] ErrorCode: %s | Msg: %s%n", ex.getErrorCode(), ex.getMessage());
        } finally {
            System.out.println("[AUDIT TRAIL] Operation attempt logged to security journal.");
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("      SECUREBANK: PHASE 4 - EXCEPTION HANDLING & VALIDATION       ");
        System.out.println("=================================================================");

        Account savings = new SavingsAccount("ACC-9001", "CUST-88", 1200.00, 0.04);

        // TEST CASE 1: Valid Withdrawal
        System.out.println("\n>>> TEST CASE 1: Normal Valid Withdrawal <<<");
        executeWithdrawal(savings, 300.00); // 1200 - 300 = 900 (Valid, > 500)

        // TEST CASE 2: Invalid Negative Amount (Fail-Fast Validator)
        System.out.println("\n>>> TEST CASE 2: Invalid Negative Amount <<<");
        executeWithdrawal(savings, -50.00);

        // TEST CASE 3: Insufficient Balance (Breaching $500 minimum balance)
        System.out.println("\n>>> TEST CASE 3: Insufficient Balance Breach <<<");
        executeWithdrawal(savings, 600.00); // Current is 900, 900 - 600 = 300 (< 500)

        // TEST CASE 4: Account Blocked
        System.out.println("\n>>> TEST CASE 4: Operation on Blocked Account <<<");
        savings.setStatus("BLOCKED");
        executeWithdrawal(savings, 100.00);

        // TEST CASE 5: Validator Testing (Email and Transfer self-check)
        System.out.println("\n>>> TEST CASE 5: Fail-Fast Form Validation <<<");
        try {
            System.out.println("Validating invalid email 'john-at-bank.com'...");
            BankingValidator.validateEmail("john-at-bank.com");
        } catch (BankingException ex) {
            System.err.printf("[VALIDATOR CAUGHT] %s -> %s%n", ex.getErrorCode(), ex.getMessage());
        }

        try {
            System.out.println("\nValidating self-transfer from ACC-9001 to ACC-9001...");
            BankingValidator.validateTransferAccounts("ACC-9001", "ACC-9001");
        } catch (BankingException ex) {
            System.err.printf("[VALIDATOR CAUGHT] %s -> %s%n", ex.getErrorCode(), ex.getMessage());
        }

        System.out.println("\n=================================================================");
        System.out.println("                PHASE 4 EXECUTION COMPLETE                       ");
        System.out.println("=================================================================");
    }
}
