package com.securebank.prototype;

/**
 * Main application runner for Phase 2: Java OOP Console Prototype.
 * Demonstrates:
 * 1. Object creation and parameter passing.
 * 2. Polymorphic method calls (Dynamic Method Dispatch).
 * 3. Encapsulation & invariant enforcement (Minimum balance, Overdraft).
 * 4. Transfer coordination between accounts.
 */
public class BankApp {
    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("         SECUREBANK: PHASE 2 - JAVA OOP CONSOLE PROTOTYPE        ");
        System.out.println("=================================================================\n");

        // 1. Create Customers
        Customer alice = new Customer("CUST1001", "Alice", "Johnson", "alice@example.com", "+1-555-0101");
        Customer bob = new Customer("CUST1002", "Bob", "Smith", "bob@example.com", "+1-555-0202");

        // 2. Instantiate Accounts demonstrating Inheritance and Polymorphism
        // Alice opens a Savings Account ($1500 initial, 4% interest, $500 min balance)
        Account aliceSavings = new SavingsAccount("ACC-SAV-1001", alice.getCustomerId(), 1500.00, 0.04);
        alice.addAccount(aliceSavings);

        // Bob opens a Current Account ($800 initial, $500 overdraft limit)
        Account bobCurrent = new CurrentAccount("ACC-CUR-2001", bob.getCustomerId(), 800.00, 500.00);
        bob.addAccount(bobCurrent);

        System.out.println("\n--- INITIAL STATE ---");
        alice.displayCustomerSummary();
        bob.displayCustomerSummary();

        // 3. Test Deposit
        System.out.println("\n--- TEST 1: DEPOSIT OPERATION ---");
        aliceSavings.deposit(500.00);

        // 4. Test Polymorphic Withdrawal on SavingsAccount (Subject to $500 minimum balance)
        System.out.println("\n--- TEST 2: SAVINGS WITHDRAWAL & MINIMUM BALANCE INVARIANT ---");
        System.out.println("Attempting valid withdrawal of $700.00 from Alice's Savings Account...");
        aliceSavings.withdraw(700.00); // Balance becomes $1300.00

        System.out.println("\nAttempting invalid withdrawal of $900.00 (would breach $500 min balance rule)...");
        aliceSavings.withdraw(900.00); // $1300 - $900 = $400 (< $500) -> MUST FAIL

        // 5. Test Polymorphic Withdrawal on CurrentAccount (Subject to Overdraft Limit)
        System.out.println("\n--- TEST 3: CURRENT ACCOUNT WITHDRAWAL & OVERDRAFT PROTECTION ---");
        System.out.println("Bob's balance is $800.00 with $500.00 overdraft limit. Total available: $1300.00.");
        System.out.println("Attempting withdrawal of $1000.00 (utilizing $200 of overdraft)...");
        bobCurrent.withdraw(1000.00); // Balance becomes -$200.00

        System.out.println("\nAttempting withdrawal of $400.00 (exceeds remaining $300 overdraft)...");
        bobCurrent.withdraw(400.00); // MUST FAIL

        // 6. Test Funds Transfer Between Alice and Bob
        System.out.println("\n--- TEST 4: INTER-ACCOUNT TRANSFER ---");
        System.out.println("Transferring $300.00 from Alice's Savings to Bob's Current Account...");
        aliceSavings.transfer(bobCurrent, 300.00);

        // 7. Test Account Status Invariant (Blocked Account)
        System.out.println("\n--- TEST 5: ACCOUNT STATUS RESTRICTIONS (BLOCKED STATE) ---");
        System.out.println("Admin blocks Bob's Current Account due to audit flags...");
        bobCurrent.setStatus("BLOCKED");
        bobCurrent.displayAccountInfo();

        System.out.println("\nAlice attempts to transfer $100.00 to Bob's BLOCKED account...");
        aliceSavings.transfer(bobCurrent, 100.00); // Should fail & rollback safely!

        System.out.println("\n--- FINAL STATE SUMMARY ---");
        alice.displayCustomerSummary();
        bob.displayCustomerSummary();
        System.out.println("=================================================================");
        System.out.println("                PHASE 2 EXECUTION COMPLETE                       ");
        System.out.println("=================================================================");
    }
}
