# 🧠 SecureBank — Core Java, OOP & DSA Prototype Module

This module contains standalone, pure Java demonstrations of core Data Structures & Algorithms applied to banking systems (without Spring Boot dependencies):

## 📊 DSA Implementations Included:

1. **`HashMap<String, Account>`** (`BankDSARegistry.java`): $O(1)$ account lookup by account number.
2. **`HashSet<String>`** (`BankDSARegistry.java`): $O(1)$ duplicate email and account deduplication.
3. **`PriorityQueue<SupportTicket>`** (`PrioritySupportQueue.java`): Min-heap for VIP customer priority routing.
4. **`LinkedList / Queue<Customer>`** (`CustomerServiceQueue.java`): FIFO queue for token-based bank teller service.
5. **MergeSort & QuickSort** (`AccountSortEngine.java`): $O(n \log n)$ balance sorting for analytical reporting.
6. **Binary Search** (`TransactionSearchEngine.java`): $O(\log n)$ transaction lookup by ID/amount on sorted ledgers.

## 🚀 How to Run

Double-click `run-dsa-demo.bat` or run:

```bash
cd dsa-prototype
javac -d bin src/com/securebank/prototype/*.java src/com/securebank/dsa/*.java src/com/securebank/exception/*.java src/com/securebank/validator/*.java src/com/securebank/io/*.java
java -cp bin com.securebank.dsa.DSAMain
```
