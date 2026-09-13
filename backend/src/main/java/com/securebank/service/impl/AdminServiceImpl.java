package com.securebank.service.impl;

import com.securebank.dto.response.AccountResponse;
import com.securebank.dto.response.BankStatisticsResponse;
import com.securebank.dto.response.CustomerResponse;
import com.securebank.dto.response.TransactionResponse;
import com.securebank.entity.Account;
import com.securebank.entity.Customer;
import com.securebank.entity.Transaction;
import com.securebank.entity.enums.AccountStatus;
import com.securebank.exception.AccountNotFoundException;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.CustomerRepository;
import com.securebank.repository.TransactionRepository;
import com.securebank.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AdminServiceImpl(CustomerRepository customerRepository, 
                            AccountRepository accountRepository, 
                            TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BankStatisticsResponse getBankStatistics() {
        long totalCustomers = customerRepository.count();
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.countByStatus(AccountStatus.ACTIVE);
        long blockedAccounts = accountRepository.countByStatus(AccountStatus.BLOCKED);
        BigDecimal vaultReserves = accountRepository.getTotalVaultReserves();
        long totalTxns = transactionRepository.count();

        return new BankStatisticsResponse(
                totalCustomers,
                totalAccounts,
                activeAccounts,
                blockedAccounts,
                vaultReserves,
                totalTxns
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapCustomerToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::mapAccountToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(this::mapTransactionToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponse blockAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        account.setStatus(AccountStatus.BLOCKED);
        Account saved = accountRepository.save(account);
        return mapAccountToResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponse unblockAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        account.setStatus(AccountStatus.ACTIVE);
        Account saved = accountRepository.save(account);
        return mapAccountToResponse(saved);
    }

    private CustomerResponse mapCustomerToResponse(Customer c) {
        CustomerResponse res = new CustomerResponse();
        res.setCustomerId(c.getCustomerId());
        res.setFirstName(c.getFirstName());
        res.setLastName(c.getLastName());
        res.setFullName(c.getFullName());
        res.setEmail(c.getEmail());
        res.setPhone(c.getPhone());
        res.setAddress(c.getAddress());
        res.setRole(c.getRole().name());
        res.setCreatedAt(c.getCreatedAt());
        return res;
    }

    private AccountResponse mapAccountToResponse(Account a) {
        AccountResponse res = new AccountResponse();
        res.setAccountId(a.getAccountId());
        res.setAccountNumber(a.getAccountNumber());
        res.setCustomerId(a.getCustomer().getCustomerId());
        res.setCustomerName(a.getCustomer().getFullName());
        res.setAccountType(a.getAccountType().name());
        res.setBalance(a.getBalance());
        res.setStatus(a.getStatus().name());
        res.setMinimumBalance(a.getMinimumBalance());
        res.setOverdraftLimit(a.getOverdraftLimit());
        res.setTotalAvailableFunds(a.getTotalAvailableFunds());
        res.setCreatedAt(a.getCreatedAt());
        return res;
    }

    private TransactionResponse mapTransactionToResponse(Transaction t) {
        TransactionResponse res = new TransactionResponse();
        res.setTransactionId(t.getTransactionId());
        res.setReferenceNumber(t.getReferenceNumber());
        res.setSourceAccountNumber(t.getSourceAccount() != null ? t.getSourceAccount().getAccountNumber() : null);
        res.setDestinationAccountNumber(t.getDestinationAccount() != null ? t.getDestinationAccount().getAccountNumber() : null);
        res.setTransactionType(t.getTransactionType().name());
        res.setAmount(t.getAmount());
        res.setBalanceAfter(t.getSourceBalanceAfter() != null ? t.getSourceBalanceAfter() : t.getDestinationBalanceAfter());
        res.setDescription(t.getDescription());
        res.setStatus(t.getStatus());
        res.setTimestamp(t.getCreatedAt());
        return res;
    }
}
