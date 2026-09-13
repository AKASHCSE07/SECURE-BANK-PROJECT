package com.securebank.service.impl;

import com.securebank.dto.request.DepositRequest;
import com.securebank.dto.request.TransferRequest;
import com.securebank.dto.request.WithdrawRequest;
import com.securebank.dto.response.TransactionResponse;
import com.securebank.entity.Account;
import com.securebank.entity.Transaction;
import com.securebank.entity.enums.AccountStatus;
import com.securebank.entity.enums.AccountType;
import com.securebank.entity.enums.TransactionType;
import com.securebank.exception.*;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import com.securebank.service.TransactionService;
import com.securebank.util.AccountNumberGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public TransactionServiceImpl(AccountRepository accountRepository, 
                                  TransactionRepository transactionRepository, 
                                  PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionResponse deposit(DepositRequest request) {
        Account account = accountRepository.findByAccountNumberWithLock(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountNumber()));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountBlockedException(account.getAccountNumber(), account.getStatus().name());
        }

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        String refNo = AccountNumberGenerator.generateTransactionReference();
        Transaction transaction = new Transaction(
                refNo,
                null,
                account,
                TransactionType.DEPOSIT,
                request.getAmount(),
                null,
                newBalance,
                request.getDescription()
        );
        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionResponse withdraw(WithdrawRequest request) {
        Account account = accountRepository.findByAccountNumberWithLock(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountNumber()));

        // Verify Transaction PIN
        if (!passwordEncoder.matches(request.getPin(), account.getCustomer().getPinHash())) {
            throw new UnauthorizedOperationException("Invalid Transaction PIN.");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountBlockedException(account.getAccountNumber(), account.getStatus().name());
        }

        // Financial Invariant Check
        BigDecimal currentBal = account.getBalance();
        BigDecimal minBal = account.getAccountType() == AccountType.SAVINGS 
                ? account.getMinimumBalance() : BigDecimal.ZERO;
        BigDecimal availableFunds = account.getAccountType() == AccountType.SAVINGS 
                ? currentBal.subtract(minBal) 
                : currentBal.add(account.getOverdraftLimit());

        if (request.getAmount().compareTo(availableFunds) > 0) {
            throw new InsufficientBalanceException(account.getAccountNumber(), 
                    request.getAmount().doubleValue(), availableFunds.doubleValue());
        }

        BigDecimal newBalance = currentBal.subtract(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        String refNo = AccountNumberGenerator.generateTransactionReference();
        Transaction transaction = new Transaction(
                refNo,
                account,
                null,
                TransactionType.WITHDRAWAL,
                request.getAmount(),
                newBalance,
                null,
                request.getDescription()
        );
        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionResponse transfer(TransferRequest request) {
        if (request.getSourceAccountNumber().equalsIgnoreCase(request.getDestinationAccountNumber())) {
            throw new UnauthorizedOperationException("Source and destination accounts cannot be identical.");
        }

        // Canonical Lock Acquisition Order to prevent Deadlocks (Alphabetical order of account numbers)
        String firstAccNo = request.getSourceAccountNumber().compareTo(request.getDestinationAccountNumber()) < 0
                ? request.getSourceAccountNumber() : request.getDestinationAccountNumber();
        String secondAccNo = request.getSourceAccountNumber().compareTo(request.getDestinationAccountNumber()) < 0
                ? request.getDestinationAccountNumber() : request.getSourceAccountNumber();

        // Lock in strict sequence
        accountRepository.findByAccountNumberWithLock(firstAccNo)
                .orElseThrow(() -> new AccountNotFoundException(firstAccNo));
        accountRepository.findByAccountNumberWithLock(secondAccNo)
                .orElseThrow(() -> new AccountNotFoundException(secondAccNo));

        // Fetch fresh references under lock
        Account sourceAcc = accountRepository.findByAccountNumber(request.getSourceAccountNumber()).get();
        Account destAcc = accountRepository.findByAccountNumber(request.getDestinationAccountNumber()).get();

        // Step-Up Auth (Verify Sender's PIN)
        if (!passwordEncoder.matches(request.getPin(), sourceAcc.getCustomer().getPinHash())) {
            throw new UnauthorizedOperationException("Invalid Transaction PIN.");
        }

        if (sourceAcc.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountBlockedException(sourceAcc.getAccountNumber(), sourceAcc.getStatus().name());
        }
        if (destAcc.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountBlockedException(destAcc.getAccountNumber(), destAcc.getStatus().name());
        }

        // Balance Check on Source
        BigDecimal sourceBal = sourceAcc.getBalance();
        BigDecimal sourceMinBal = sourceAcc.getAccountType() == AccountType.SAVINGS 
                ? sourceAcc.getMinimumBalance() : BigDecimal.ZERO;
        BigDecimal availableFunds = sourceAcc.getAccountType() == AccountType.SAVINGS 
                ? sourceBal.subtract(sourceMinBal) 
                : sourceBal.add(sourceAcc.getOverdraftLimit());

        if (request.getAmount().compareTo(availableFunds) > 0) {
            throw new InsufficientBalanceException(sourceAcc.getAccountNumber(), 
                    request.getAmount().doubleValue(), availableFunds.doubleValue());
        }

        // Update Balances
        BigDecimal newSourceBal = sourceBal.subtract(request.getAmount());
        BigDecimal newDestBal = destAcc.getBalance().add(request.getAmount());

        sourceAcc.setBalance(newSourceBal);
        destAcc.setBalance(newDestBal);

        accountRepository.save(sourceAcc);
        accountRepository.save(destAcc);

        // Record Transaction
        String refNo = AccountNumberGenerator.generateTransactionReference();
        Transaction transaction = new Transaction(
                refNo,
                sourceAcc,
                destAcc,
                TransactionType.TRANSFER,
                request.getAmount(),
                newSourceBal,
                newDestBal,
                request.getDescription()
        );
        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAccountTransactions(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        return transactionRepository.findByAccountId(account.getAccountId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> filterTransactions(String accountNumber, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByAccountNumberAndDateRange(accountNumber, start, end)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction txn) {
        TransactionResponse res = new TransactionResponse();
        res.setTransactionId(txn.getTransactionId());
        res.setReferenceNumber(txn.getReferenceNumber());
        res.setSourceAccountNumber(txn.getSourceAccount() != null ? txn.getSourceAccount().getAccountNumber() : null);
        res.setDestinationAccountNumber(txn.getDestinationAccount() != null ? txn.getDestinationAccount().getAccountNumber() : null);
        res.setTransactionType(txn.getTransactionType().name());
        res.setAmount(txn.getAmount());
        res.setBalanceAfter(txn.getSourceBalanceAfter() != null ? txn.getSourceBalanceAfter() : txn.getDestinationBalanceAfter());
        res.setDescription(txn.getDescription());
        res.setStatus(txn.getStatus());
        res.setTimestamp(txn.getCreatedAt());
        return res;
    }
}
