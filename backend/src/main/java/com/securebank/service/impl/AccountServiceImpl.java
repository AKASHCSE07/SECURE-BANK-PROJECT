package com.securebank.service.impl;

import com.securebank.dto.request.CreateAccountRequest;
import com.securebank.dto.response.AccountResponse;
import com.securebank.entity.Account;
import com.securebank.entity.Customer;
import com.securebank.entity.enums.AccountStatus;
import com.securebank.entity.enums.AccountType;
import com.securebank.exception.AccountNotFoundException;
import com.securebank.exception.BankingException;
import com.securebank.exception.CustomerNotFoundException;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.CustomerRepository;
import com.securebank.service.AccountService;
import com.securebank.util.AccountNumberGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountServiceImpl(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(String.valueOf(request.getCustomerId())));

        String accNo = AccountNumberGenerator.generateAccountNumber();
        while (accountRepository.existsByAccountNumber(accNo)) {
            accNo = AccountNumberGenerator.generateAccountNumber();
        }

        BigDecimal minBal = request.getAccountType() == AccountType.SAVINGS 
                ? new BigDecimal("500.00") : BigDecimal.ZERO;
        BigDecimal overdraft = request.getAccountType() == AccountType.CURRENT 
                ? new BigDecimal("1000.00") : BigDecimal.ZERO;

        BigDecimal initialDeposit = request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO;
        if (request.getAccountType() == AccountType.SAVINGS && initialDeposit.compareTo(minBal) < 0) {
            throw new BankingException("Initial deposit for savings account must be at least $" + minBal, "INSUFFICIENT_INITIAL_DEPOSIT");
        }

        Account account = new Account(accNo, customer, request.getAccountType(), initialDeposit, minBal, overdraft);
        Account savedAccount = accountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        return account.getBalance();
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(String accountNumber, String statusStr) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        try {
            AccountStatus newStatus = AccountStatus.valueOf(statusStr.toUpperCase());
            account.setStatus(newStatus);
            Account saved = accountRepository.save(account);
            return mapToResponse(saved);
        } catch (IllegalArgumentException e) {
            throw new BankingException("Invalid account status: " + statusStr + ". Valid values are ACTIVE, BLOCKED, CLOSED.", "INVALID_STATUS");
        }
    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountNumber(account.getAccountNumber());
        response.setCustomerId(account.getCustomer().getCustomerId());
        response.setCustomerName(account.getCustomer().getFullName());
        response.setAccountType(account.getAccountType().name());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus().name());
        response.setMinimumBalance(account.getMinimumBalance());
        response.setOverdraftLimit(account.getOverdraftLimit());
        response.setTotalAvailableFunds(account.getTotalAvailableFunds());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }
}
