# 🛡️ SecureBank – Full-Stack Banking Management System

> **Academic Full-Stack Project** | BTech Computer Science | Java + DSA Summer Internship

A production-grade, enterprise-level digital banking platform built with **Java 17, Spring Boot 3, Spring Security, JWT, PostgreSQL, and Vanilla JavaScript**. This project demonstrates advanced OOP principles, core Data Structures & Algorithms, ACID transaction management, concurrent fund transfer safety, and modern REST API design.

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [System Architecture](#system-architecture)
4. [Data Structures & Algorithms Showcase](#data-structures--algorithms-showcase)
5. [OOP Principles Applied](#oop-principles-applied)
6. [Database Schema & Design](#database-schema--design)
7. [REST API Reference](#rest-api-reference)
8. [Security Design](#security-design)
9. [Concurrency & ACID Safety](#concurrency--acid-safety)
10. [Project Setup Guide](#project-setup-guide)
11. [Running the Application](#running-the-application)
12. [Testing Guide](#testing-guide)
13. [Frontend Pages](#frontend-pages)
14. [Project Structure](#project-structure)

---

## 🎯 Project Overview

SecureBank solves four critical real-world banking engineering problems:

| Problem | Solution Applied |
| :--- | :--- |
| **Race Conditions in Concurrent Transfers** | `SELECT ... FOR UPDATE` row-level locking via `@Lock(LockModeType.PESSIMISTIC_WRITE)` |
| **Double-Spending / Overdraft Attacks** | Atomic `@Transactional` service methods with `rollbackFor = Exception.class` |
| **Deadlocks in Multi-Account Transfers** | Canonical lock ordering (lexicographic min-account-first acquisition) |
| **Password & PIN Theft** | BCrypt Work Factor 12 hashing; JWT Bearer stateless tokens; Step-Up PIN for debits |

### Core Feature Set

- ✅ **Customer Self-Service Portal**: Registration, multi-account management, balance inquiry
- ✅ **Atomic Deposits, Withdrawals & P2P Transfers** with Step-Up PIN validation
- ✅ **Real-Time Double-Entry Transaction Ledger** with timestamped reference numbers
- ✅ **Beneficiary / Saved Payee Management**
- ✅ **Admin Governance Suite**: Bank-wide vault metrics, customer directory, account freeze/unblock
- ✅ **Role-Based Access Control (RBAC)**: `ROLE_CUSTOMER` vs `ROLE_ADMIN`
- ✅ **Global Audit Stream**: Complete immutable transaction history for compliance
- ✅ **Responsive Web UI**: Glassmorphism design, CSS3 Grid/Flexbox, mobile-first

---

## 💻 Technology Stack

### Backend
| Layer | Technology | Version |
| :--- | :--- | :--- |
| Language | Java | 17+ |
| Framework | Spring Boot | 3.2.3 |
| ORM | Spring Data JPA / Hibernate | 6.x |
| Security | Spring Security + JWT (JJWT) | 6.x / 0.11.x |
| Database Driver | PostgreSQL JDBC | 42.x |
| Connection Pool | HikariCP | 5.x |
| Validation | Jakarta Bean Validation | 3.x |
| Build Tool | Apache Maven | 3.8+ |

### Frontend
| Layer | Technology |
| :--- | :--- |
| Markup | HTML5 (Semantic) |
| Styling | Vanilla CSS3 (Grid, Flexbox, Custom Properties) |
| Logic | Vanilla JavaScript ES6+ (Fetch API, async/await) |
| Fonts | Google Fonts – Inter |

### Database & DevOps
| Tool | Purpose |
| :--- | :--- |
| PostgreSQL 15 | Relational ACID-compliant persistent store |
| Postman | REST API manual + automated testing |
| Git + GitHub | Source control and collaborative workflow |
| IntelliJ IDEA / VS Code | IDE environments |

---

## 🏛️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         SECUREBANK ARCHITECTURE                      │
├─────────────────────────────────────────────────────────────────────┤
│  Browser Client (HTML5 + CSS3 + Vanilla JS)                          │
│    │                                                                 │
│    │  HTTP Requests + Authorization: Bearer <JWT>                    │
│    ▼                                                                 │
│  Spring Boot 3 (Tomcat Embedded Server, Port 8080)                   │
│    │                                                                 │
│    ├── JwtAuthenticationFilter (OncePerRequestFilter)                │
│    ├── @RestController Layer (CustomerController, etc.)              │
│    ├── @Service Layer (TransactionServiceImpl, etc.)                 │
│    ├── @Repository Layer (Spring Data JPA + JPQL)                    │
│    └── @Entity Layer (Customer, Account, Transaction, etc.)          │
│                │                                                     │
│                ▼ JDBC over HikariCP Connection Pool                  │
│  PostgreSQL 15 Database                                              │
│    ├── customers | accounts | transactions                           │
│    ├── beneficiaries | admins | audit_logs                           │
│    └── B-Tree Indexes on accountNumber, email                        │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Data Structures & Algorithms Showcase

This project meaningfully demonstrates every core DSA concept with real banking use cases:

| Data Structure | File | Banking Use Case |
| :--- | :--- | :--- |
| `HashMap<String, Account>` | `BankDSARegistry.java` | O(1) account lookups by account number |
| `HashSet<String>` | `BankDSARegistry.java` | O(1) duplicate email/account deduplication |
| `ArrayList<Transaction>` | `BankDSARegistry.java` | Dynamic append-only transaction ledger |
| `ArrayDeque<String>` (Stack) | `BankDSARegistry.java` | LIFO undo-action history |
| `LinkedList<Customer>` (Queue) | `CustomerServiceQueue.java` | FIFO branch service queue |
| `PriorityQueue<Customer>` (Min-Heap) | `PrioritySupportQueue.java` | Priority support by account tier and balance |
| Binary Search | `TransactionSearchEngine.java` | O(log n) transaction lookup on sorted ledger |
| Linear Search | `TransactionSearchEngine.java` | O(n) unsorted scan fallback |
| Merge Sort | `AccountSortEngine.java` | O(n log n) sort accounts by balance |

### DSA Complexity Summary

```
HashMap Lookup  : O(1) average case    (vs O(n) linear scan in ArrayList)
Binary Search   : O(log n)             (vs O(n) linear in sorted ledger)
MergeSort       : O(n log n)           (Stable sort, preferred over QuickSort for linked data)
PriorityQueue   : O(log n) enqueue/dequeue  (Binary Min-Heap internally)
```

---

## 🧩 OOP Principles Applied

### 1. Abstraction
```java
// Account.java — Abstract class defining banking contract
public abstract class Account {
    public abstract boolean withdraw(double amount); // Each subtype enforces its own policy
    public abstract double getMinimumBalance();
}
```

### 2. Inheritance & Polymorphism
```java
// SavingsAccount enforces $500 minimum balance
// CurrentAccount enforces $1,000 overdraft protection
// Runtime dispatch calls correct withdraw() via polymorphism
Account account = new SavingsAccount("SB-1001", alice, 1500.00);
account.withdraw(300.00); // Calls SavingsAccount.withdraw(), not CurrentAccount
```

### 3. Encapsulation
```java
// Balance is private; only internal business methods can modify it
private double balance;
public double getBalance() { return balance; } // Read-only external access
```

### 4. Composition
```java
// Customer HAS-A List<Account> (Composition, not Inheritance)
public class Customer {
    private List<Account> accounts = new ArrayList<>();
}
```

---

## 🗄️ Database Schema & Design

### Entity-Relationship Model

```
customers (1) ──────────────── (M) accounts
    │                                  │
    │                                  │ (1)
    │                                  │
    └── (M) beneficiaries         (M) transactions
                                       │
                                  (FK: source_acc, dest_acc)
```

### Table Descriptions

| Table | Key Columns | Constraints |
| :--- | :--- | :--- |
| `customers` | `customer_id`, `email`, `password_hash`, `pin_hash` | `UNIQUE(email)`, `NOT NULL` |
| `accounts` | `account_number`, `customer_id`, `balance`, `status` | `CHECK (balance >= 0)`, `FK(customer_id)` |
| `transactions` | `reference_number`, `source_acc`, `dest_acc`, `amount` | `CHECK (amount > 0)`, `NOT NULL(timestamp)` |
| `beneficiaries` | `beneficiary_id`, `customer_id`, `account_number` | `FK(customer_id) ON DELETE CASCADE` |
| `admins` | `admin_id`, `username`, `email`, `role` | `UNIQUE(username, email)` |

### Key PostgreSQL Features Used
- `SERIAL PRIMARY KEY` – Auto-incrementing IDs
- `CHECK` constraints – Enforce `balance >= 0`, `amount > 0.01`
- `ON DELETE RESTRICT` – Prevent orphaned account records
- `B-Tree Indexes` – on `account_number`, `email`, `timestamp` for O(log n) queries
- `TIMESTAMP WITH TIME ZONE` – Accurate multi-timezone audit timestamps

---

## 🌐 REST API Reference

All authenticated endpoints require: `Authorization: Bearer <JWT_TOKEN>`

### Authentication (`/api/auth`)
| Method | Endpoint | Body | Response |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | `{firstName, lastName, email, password, pin, phone, address}` | `201 Created` + CustomerResponse |
| `POST` | `/api/auth/login` | `{email, password}` | `200 OK` + AuthResponse (JWT token) |
| `POST` | `/api/auth/admin/login` | `{email, password}` | `200 OK` + AuthResponse (Admin JWT) |

### Accounts (`/api/accounts`) — Authenticated
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/accounts` | Open a new savings or current account |
| `GET` | `/api/accounts/{accountNumber}` | Get account details by account number |
| `GET` | `/api/accounts/customer/{customerId}` | Get all accounts for a customer |
| `GET` | `/api/accounts/{accountNumber}/balance` | Live balance inquiry |
| `PATCH` | `/api/accounts/{accountNumber}/status` | Update account status |

### Transactions (`/api/transactions`) — Authenticated
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/transactions/deposit` | Deposit funds to account |
| `POST` | `/api/transactions/withdraw` | Withdraw funds with Step-Up PIN |
| `POST` | `/api/transfers` | Execute P2P transfer with Step-Up PIN |
| `GET` | `/api/transactions/account/{accountNumber}` | Full statement history |
| `GET` | `/api/transactions/account/{accountNumber}/filter` | Date-range filtered statement |

### Admin (`/api/admin`) — `ROLE_ADMIN` Only
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/admin/statistics` | Total vault reserves, account counts, metrics |
| `GET` | `/api/admin/customers` | Full customer directory |
| `GET` | `/api/admin/accounts` | All accounts in system |
| `GET` | `/api/admin/transactions` | Global immutable audit stream |
| `PATCH` | `/api/admin/accounts/{accountNumber}/block` | Freeze / block an account |
| `PATCH` | `/api/admin/accounts/{accountNumber}/unblock` | Unfreeze / restore an account |

### Error Response Format
```json
{
  "success": false,
  "message": "Insufficient funds: current balance $450.00 is below the minimum of $500.00",
  "errorCode": "INSUFFICIENT_BALANCE",
  "details": "uri=/api/transactions/withdraw",
  "timestamp": "2026-08-14T01:00:00"
}
```

---

## 🔐 Security Design

### Authentication Flow
```
1. Customer POSTs credentials to POST /api/auth/login
2. AuthenticationManager verifies BCrypt(password) against stored hash
3. JwtTokenProvider generates HMAC-SHA256 signed JWT (24-hour expiry)
4. Client stores JWT in localStorage
5. Every subsequent request includes: Authorization: Bearer <JWT>
6. JwtAuthenticationFilter validates signature, expiry, and sets SecurityContext
```

### Password & PIN Protection
| Credential | Hashing Algorithm | Work Factor | Rationale |
| :--- | :--- | :--- | :--- |
| Login Password | BCrypt | 12 (4,096 rounds) | Brute-force resistant, auto-salted |
| Transaction PIN | BCrypt | 12 (4,096 rounds) | Step-Up re-authentication for debits |

### Security Invariants
- ❌ Raw passwords or PINs are **never** logged or stored in plaintext
- ❌ JWT secret is **never** committed to Git (loaded from `application.properties`)
- ✅ CORS configured to allow only `localhost:3000` origins (configurable per environment)
- ✅ CSRF disabled (stateless REST API — no cookies, no CSRF surface)
- ✅ `SessionCreationPolicy.STATELESS` — zero server-side session memory

---

## 🔒 Concurrency & ACID Safety

### Deadlock Prevention in Fund Transfers

```java
// TransactionServiceImpl.java — Canonical lock ordering
String firstAccNo = sourceAcc.compareTo(destAcc) < 0 ? sourceAcc : destAcc;
String secondAccNo = sourceAcc.compareTo(destAcc) < 0 ? destAcc : sourceAcc;

Account first = accountRepository.findByAccountNumberWithLock(firstAccNo).orElseThrow(...);
Account second = accountRepository.findByAccountNumberWithLock(secondAccNo).orElseThrow(...);
```

**Why this works**: Both concurrent Thread A (`Alice → Bob`) and Thread B (`Bob → Alice`) always acquire the `Alice` lock first (alphabetically smaller), so Thread B waits for Thread A to release — eliminating circular wait, which is the root cause of deadlocks.

### ACID Transaction Guarantees
| Property | Mechanism |
| :--- | :--- |
| **Atomicity** | `@Transactional(rollbackFor = Exception.class)` — all-or-nothing |
| **Consistency** | `CHECK` constraints, JPA entity validators, `InsufficientBalanceException` |
| **Isolation** | `SELECT ... FOR UPDATE` prevents phantom reads during concurrent transfers |
| **Durability** | PostgreSQL WAL (Write-Ahead Logging) ensures committed data survives crashes |

---

## ⚙️ Project Setup Guide

### Prerequisites

| Tool | Minimum Version | Download |
| :--- | :--- | :--- |
| Java JDK | 17+ | [adoptium.net](https://adoptium.net) |
| Apache Maven | 3.8+ | [maven.apache.org](https://maven.apache.org) |
| PostgreSQL | 14+ | [postgresql.org](https://postgresql.org) |
| Git | 2.x | [git-scm.com](https://git-scm.com) |
| Postman | Latest | [postman.com](https://postman.com) |

---

## 🖥️ Localhost Setup (Windows)

> **Architecture**: Browser → HTML/JS frontend → Spring Boot API (port 8080) → PostgreSQL (port 5432)
> **Note**: This project has NO Node.js component. Only Java + Spring Boot is needed.

---

### 📦 Required Software

| Tool | Version Required | Verify With |
| :--- | :--- | :--- |
| **Java JDK 17** | 17+ (LTS) | `java --version` |
| **Apache Maven** | 3.8+ | `mvn --version` |
| **PostgreSQL** | 14 or 15 | `psql --version` |
| Git | 2.x (already installed) | `git --version` |

---

### 🔧 Installation Steps (Windows)

#### 1. Install Java JDK 17

1. Go to: https://adoptium.net/temurin/releases/?version=17
2. Download: **Windows x64 `.msi` installer** (Temurin 17 LTS)
3. Run the installer — **tick the checkbox** "Set JAVA_HOME variable"
4. Verify: open a **new** PowerShell window and run `java --version`
   - Expected: `openjdk 17.x.x ...`

#### 2. Install Apache Maven 3.8+

1. Go to: https://maven.apache.org/download.cgi
2. Download: **Binary zip archive** (`apache-maven-3.x.x-bin.zip`)
3. Extract to: `C:\maven\`
4. Add `C:\maven\bin` to your Windows PATH:
   - Search "Edit the system environment variables" → Environment Variables
   - Under "System variables" → select `Path` → Edit → New → `C:\maven\bin`
5. Open a **new** PowerShell and run: `mvn --version`
   - Expected: `Apache Maven 3.x.x ...`

#### 3. Install PostgreSQL 15

1. Go to: https://www.postgresql.org/download/windows/
2. Download and run the **EDB installer for PostgreSQL 15**
3. During install:
   - Set a password for the `postgres` superuser (remember this!)
   - Keep default port: **5432**
4. After install, the PostgreSQL service starts automatically
5. Verify: `psql --version` → Expected: `psql (PostgreSQL) 15.x`

---

### 🗄️ Database Setup

**Option A — pgAdmin 4 (Graphical, recommended):**
1. Open pgAdmin 4 → right-click "Databases" → Create → Database
2. Name: `securebank_db` → Save
3. Right-click `securebank_db` → Query Tool
4. Open and run: `database/schema.sql`
5. Open and run: `database/seed_data_fixed.sql`

**Option B — psql (Command Line):**
```powershell
psql -U postgres -c "CREATE DATABASE securebank_db;"
psql -U postgres -d securebank_db -f "c:\Users\HP\Documents\Bankproject\database\schema.sql"
psql -U postgres -d securebank_db -f "c:\Users\HP\Documents\Bankproject\database\seed_data_fixed.sql"
```

> ⚠️ **Use `seed_data_fixed.sql`** (not the original `seed_data.sql`).
> The original has non-functional placeholder BCrypt hashes; the fixed file has valid hashes.

---

### ⚙️ Configure `application.properties`

Edit `backend/src/main/resources/application.properties` — update **only** the password line:

```properties
spring.datasource.password=YOUR_POSTGRES_PASSWORD_HERE
```

Everything else is already configured for local development.

---

### 🚀 Running the Application

#### Terminal 1 — Start Spring Boot Backend

```powershell
cd c:\Users\HP\Documents\Bankproject\backend
mvn spring-boot:run
```

Wait for: `Started SecureBankApplication in X.XXX seconds`

The API is now live at: **http://localhost:8080**

#### Open the Frontend

**Option A (Recommended) — VS Code Live Server:**
1. Open the `frontend/` folder in VS Code
2. Right-click `index.html` → "Open with Live Server"
3. Browser opens at: `http://localhost:5500`

**Option B — File directly:**
- Double-click `frontend/index.html` in File Explorer
- Uses `file://` protocol — works fine since CORS is `*`

**Option C — Batch file:**
- Double-click `start-project.bat` in the project root

---

### 🔑 Seed User Credentials (use `seed_data_fixed.sql`)

| Role | Email / Username | Password | PIN |
| :--- | :--- | :--- | :--- |
| Customer | `alice@securebank.com` | `Cust@123` | `1234` |
| Customer | `bob@securebank.com` | `Cust@123` | `1234` |
| Admin | `admin@securebank.com` | `Admin@123` | — |

**Expected startup log:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

[SecureBank] 🚀 Spring Boot Application Started on port 8080
[SecureBank] ✅ Database connection established via HikariCP
[SecureBank] ✅ SecurityFilterChain initialized with JWT stateless mode
```

### Open the Frontend

Open `frontend/index.html` in your browser, or use VS Code **Live Server** extension.

### Seed User Credentials (from `seed_data.sql`)

| Role | Email / Username | Password | PIN |
| :--- | :--- | :--- | :--- |
| Customer | `alice@securebank.com` | `Cust@123` | `1234` |
| Customer | `bob@securebank.com` | `Cust@123` | `1234` |
| Admin | `admin@securebank.com` | `Admin@123` | — |

---

## 🧪 Testing Guide

### Run JUnit 5 Unit Tests

```bash
cd c:\Users\HP\Documents\Bankproject\backend
mvn test
```

### Postman API Collection

1. Open Postman.
2. Click **Import** → Select `backend/postman/SecureBank_Postman_Collection.json`.
3. Click **Run Collection** to execute all 11 scenarios.

### Manual Test Scenarios

| # | Scenario | Expected HTTP Status |
| :--- | :--- | :--- |
| 1 | Register new customer | `201 Created` |
| 2 | Login with correct credentials | `200 OK` + JWT |
| 3 | Login with wrong password | `401 Unauthorized` |
| 4 | Deposit `$500` to Savings account | `200 OK` |
| 5 | Withdraw with wrong PIN | `401 Unauthorized` |
| 6 | Withdraw more than available balance | `422 Unprocessable Entity` |
| 7 | Transfer to blocked account | `403 Forbidden` |
| 8 | Access `/api/admin/**` as Customer | `403 Forbidden` |
| 9 | Access protected endpoint without JWT | `401 Unauthorized` |
| 10 | Admin blocks account | `200 OK` + status = BLOCKED |

---

## 📄 Frontend Pages

| Page | File | Description |
| :--- | :--- | :--- |
| Landing Page | `frontend/index.html` | Hero banner & product features |
| Login | `frontend/login.html` | Tabbed Customer/Admin portal login |
| Register | `frontend/register.html` | New customer account onboarding |
| Customer Dashboard | `frontend/customer-dashboard.html` | Balance, transactions, deposit/withdraw/transfer |
| Admin Portal | `frontend/admin-dashboard.html` | Metrics, account moderation, audit stream |

---

## 📁 Project Structure

```
c:\Users\HP\Documents\Bankproject\
├── .gitignore
│
├── database/
│   ├── schema.sql                        ← PostgreSQL DDL + Indexes
│   ├── seed_data.sql                     ← Test fixture data
│   └── queries.sql                       ← Named analytical queries
│
├── src/                                  ← Phase 2–5: Java Prototypes
│   └── com/securebank/
│       ├── prototype/                    ← OOP: Account, Customer, BankApp
│       ├── dsa/                          ← DSA Engine: HashMap, PriorityQueue, MergeSort
│       ├── exception/                    ← Custom Exception Hierarchy
│       ├── validator/                    ← BankingValidator
│       └── io/                           ← File I/O: Ledger & Statement Exporter
│
├── backend/                              ← Spring Boot Application
│   ├── pom.xml
│   ├── postman/
│   │   └── SecureBank_Postman_Collection.json
│   └── src/
│       ├── main/
│       │   ├── java/com/securebank/
│       │   │   ├── SecureBankApplication.java
│       │   │   ├── config/               ← SecurityConfig, CorsConfig
│       │   │   ├── controller/           ← REST Controllers
│       │   │   ├── dto/                  ← Request & Response DTOs
│       │   │   ├── entity/               ← JPA Entities + Enums
│       │   │   ├── exception/            ← GlobalExceptionHandler
│       │   │   ├── repository/           ← Spring Data JPA Repos
│       │   │   ├── security/             ← JWT Filter, Entry Point
│       │   │   ├── service/              ← Business Logic Services
│       │   │   └── util/                 ← AccountNumberGenerator
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/securebank/
│               └── service/
│                   ├── AccountServiceTest.java
│                   └── TransactionServiceTest.java
│
└── frontend/                             ← Responsive Web Client
    ├── index.html
    ├── login.html
    ├── register.html
    ├── customer-dashboard.html
    ├── admin-dashboard.html
    ├── css/
    │   ├── style.css
    │   ├── login.css
    │   ├── dashboard.css
    │   └── responsive.css
    └── js/
        ├── api.js
        ├── auth.js
        ├── dashboard.js
        └── admin.js
```

---

## 👨‍🎓 Academic Note

This project was built as a **step-by-step educational journey** across **20 structured phases**, designed to teach:

- **Core Java OOP**: Abstraction, Encapsulation, Inheritance, Polymorphism
- **Data Structures**: HashMap, HashSet, ArrayList, Stack (ArrayDeque), Queue, PriorityQueue (Min-Heap)
- **Algorithms**: Linear Search, Binary Search (O(log n)), Merge Sort (O(n log n))
- **Database Engineering**: PostgreSQL relational design, ACID properties, indexing strategy
- **Enterprise Patterns**: DTO, Repository, Service, Controller, Global Exception Handler
- **Security Engineering**: BCrypt hashing, JWT stateless auth, RBAC, deadlock prevention
- **Frontend Engineering**: Responsive CSS3, Fetch API, async/await, session management

---

*Built with ❤️ for the Java + DSA Internship Program | SecureBank Academic Edition v1.0*
