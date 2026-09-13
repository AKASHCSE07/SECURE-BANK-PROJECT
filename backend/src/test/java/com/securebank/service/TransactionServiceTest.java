package com.securebank.service;

import com.securebank.dto.request.DepositRequest;
import com.securebank.dto.request.TransferRequest;
import com.securebank.dto.request.WithdrawRequest;
import com.securebank.dto.response.TransactionResponse;
import com.securebank.entity.Account;
import com.securebank.entity.Customer;
import com.securebank.entity.Transaction;
import com.securebank.entity.enums.AccountStatus;
import com.securebank.entity.enums.AccountType;
import com.securebank.exception.AccountBlockedException;
import com.securebank.exception.InsufficientBalanceException;
import com.securebank.exception.UnauthorizedOperationException;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import com.securebank.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Customer alice;
    private Customer bob;
    private Account aliceSavings;
    private Account bobCurrent;

    @BeforeEach
    void setUp() {
        alice = new Customer("Alice", "Johnson", "alice@example.com", "passHash", "pinHash", "+111", "Addr 1");
        bob = new Customer("Bob", "Smith", "bob@example.com", "passHash", "pinHash", "+222", "Addr 2");

        aliceSavings = new Account("SB-1001-SAV", alice, AccountType.SAVINGS, new BigDecimal("2000.00"), new BigDecimal("500.00"), BigDecimal.ZERO);
        bobCurrent = new Account("SB-2001-CUR", bob, AccountType.CURRENT, new BigDecimal("800.00"), BigDecimal.ZERO, new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Should successfully deposit money and update account balance")
    void testDeposit_Success() {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("SB-1001-SAV");
        req.setAmount(new BigDecimal("500.00"));
        req.setDescription("Payroll Deposit");

        when(accountRepository.findByAccountNumberWithLock("SB-1001-SAV")).thenReturn(Optional.of(aliceSavings));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        TransactionResponse res = transactionService.deposit(req);

        assertNotNull(res);
        assertEquals("DEPOSIT", res.getTransactionType());
        assertEquals(new BigDecimal("2500.00"), aliceSavings.getBalance());
        verify(accountRepository, times(1)).save(aliceSavings);
    }

    @Test
    @DisplayName("Should successfully withdraw funds when PIN is valid and balance is sufficient")
    void testWithdraw_Success() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountNumber("SB-1001-SAV");
        req.setAmount(new BigDecimal("400.00"));
        req.setPin("1234");
        req.setDescription("ATM Cash");

        when(accountRepository.findByAccountNumberWithLock("SB-1001-SAV")).thenReturn(Optional.of(aliceSavings));
        when(passwordEncoder.matches("1234", "pinHash")).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        TransactionResponse res = transactionService.withdraw(req);

        assertNotNull(res);
        assertEquals(new BigDecimal("1600.00"), aliceSavings.getBalance());
    }

    @Test
    @DisplayName("Should reject withdrawal when Transaction PIN is incorrect")
    void testWithdraw_InvalidPin() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountNumber("SB-1001-SAV");
        req.setAmount(new BigDecimal("100.00"));
        req.setPin("9999"); // Wrong PIN

        when(accountRepository.findByAccountNumberWithLock("SB-1001-SAV")).thenReturn(Optional.of(aliceSavings));
        when(passwordEncoder.matches("9999", "pinHash")).thenReturn(false);

        assertThrows(UnauthorizedOperationException.class, () -> transactionService.withdraw(req));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully execute atomic P2P transfer between two accounts")
    void testTransfer_Success() {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountNumber("SB-1001-SAV");
        req.setDestinationAccountNumber("SB-2001-CUR");
        req.setAmount(new BigDecimal("500.00"));
        req.setPin("1234");
        req.setDescription("Rent share");

        when(accountRepository.findByAccountNumberWithLock("SB-1001-SAV")).thenReturn(Optional.of(aliceSavings));
        when(accountRepository.findByAccountNumberWithLock("SB-2001-CUR")).thenReturn(Optional.of(bobCurrent));
        when(accountRepository.findByAccountNumber("SB-1001-SAV")).thenReturn(Optional.of(aliceSavings));
        when(accountRepository.findByAccountNumber("SB-2001-CUR")).thenReturn(Optional.of(bobCurrent));
        when(passwordEncoder.matches("1234", "pinHash")).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        TransactionResponse res = transactionService.transfer(req);

        assertNotNull(res);
        assertEquals(new BigDecimal("1500.00"), aliceSavings.getBalance()); // 2000 - 500
        assertEquals(new BigDecimal("1300.00"), bobCurrent.getBalance());   // 800 + 500
        verify(accountRepository, times(1)).save(aliceSavings);
        verify(accountRepository, times(1)).save(bobCurrent);
    }

    @Test
    @DisplayName("Should reject transfer when source account is BLOCKED")
    void testTransfer_BlockedSourceAccount() {
        aliceSavings.setStatus(AccountStatus.BLOCKED);

        TransferRequest req = new TransferRequest();
        req.setSourceAccountNumber("SB-1001-SAV");
        req.setDestinationAccountNumber("SB-2001-CUR");
        req.setAmount(new BigDecimal("100.00"));
        req.setPin("1234");

        when(accountRepository.findByAccountNumberWithLock("SB-1001-SAV")).thenReturn(Optional.of(aliceSavings));
        when(accountRepository.findByAccountNumberWithLock("SB-2001-CUR")).thenReturn(Optional.of(bobCurrent));
        when(accountRepository.findByAccountNumber("SB-1001-SAV")).thenReturn(Optional.of(aliceSavings));
        when(accountRepository.findByAccountNumber("SB-2001-CUR")).thenReturn(Optional.of(bobCurrent));
        when(passwordEncoder.matches("1234", "pinHash")).thenReturn(true);

        assertThrows(AccountBlockedException.class, () -> transactionService.transfer(req));
        verify(accountRepository, never()).save(any());
    }
}
