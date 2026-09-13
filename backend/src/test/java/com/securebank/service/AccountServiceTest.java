package com.securebank.service;

import com.securebank.dto.request.CreateAccountRequest;
import com.securebank.dto.response.AccountResponse;
import com.securebank.entity.Account;
import com.securebank.entity.Customer;
import com.securebank.entity.enums.AccountType;
import com.securebank.exception.BankingException;
import com.securebank.exception.CustomerNotFoundException;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.CustomerRepository;
import com.securebank.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Customer mockCustomer;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer("Alice", "Johnson", "alice@example.com", "hash", "pinhash", "+123456789", "Street 1");
        mockCustomer.setCustomerId(1L);
    }

    @Test
    @DisplayName("Should successfully create a Savings Account when initial deposit >= $500")
    void testCreateSavingsAccount_Success() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(1L);
        request.setAccountType(AccountType.SAVINGS);
        request.setInitialDeposit(new BigDecimal("1000.00"));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            acc.setAccountId(101L);
            return acc;
        });

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals("SAVINGS", response.getAccountType());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw BankingException when Savings initial deposit is less than $500")
    void testCreateSavingsAccount_InsufficientInitialDeposit() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(1L);
        request.setAccountType(AccountType.SAVINGS);
        request.setInitialDeposit(new BigDecimal("200.00")); // Less than $500 min balance

        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));

        assertThrows(BankingException.class, () -> accountService.createAccount(request));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer does not exist")
    void testCreateAccount_CustomerNotFound() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(999L);
        request.setAccountType(AccountType.SAVINGS);

        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> accountService.createAccount(request));
    }
}
