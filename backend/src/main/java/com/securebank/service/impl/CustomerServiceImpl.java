package com.securebank.service.impl;

import com.securebank.dto.request.CustomerRegisterRequest;
import com.securebank.dto.response.CustomerResponse;
import com.securebank.entity.Account;
import com.securebank.entity.Customer;
import com.securebank.entity.enums.AccountType;
import com.securebank.exception.CustomerNotFoundException;
import com.securebank.exception.DuplicateResourceException;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.CustomerRepository;
import com.securebank.service.CustomerService;
import com.securebank.util.AccountNumberGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(CustomerRepository customerRepository, 
                               AccountRepository accountRepository, 
                               PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public CustomerResponse registerCustomer(CustomerRegisterRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer", "Email: " + request.getEmail());
        }
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Customer", "Phone: " + request.getPhone());
        }

        // Hash Password & Transaction PIN securely
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String encodedPin = passwordEncoder.encode(request.getPin());

        Customer customer = new Customer(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                encodedPassword,
                encodedPin,
                request.getPhone(),
                request.getAddress()
        );

        Customer savedCustomer = customerRepository.save(customer);

        // Auto-provision initial Savings Account with $500.00 minimum balance
        String newAccNo = AccountNumberGenerator.generateAccountNumber();
        while (accountRepository.existsByAccountNumber(newAccNo)) {
            newAccNo = AccountNumberGenerator.generateAccountNumber();
        }

        Account defaultSavings = new Account(
                newAccNo,
                savedCustomer,
                AccountType.SAVINGS,
                new BigDecimal("500.00"), // Initial balance
                new BigDecimal("500.00"), // Minimum balance
                BigDecimal.ZERO           // Overdraft limit
        );
        accountRepository.save(defaultSavings);

        CustomerResponse response = mapToResponse(savedCustomer);
        response.setAccountNumber(newAccNo);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(String.valueOf(customerId)));
        return mapToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Email: " + email));
        return mapToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyPin(Long customerId, String plainPin) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(String.valueOf(customerId)));
        return passwordEncoder.matches(plainPin, customer.getPinHash());
    }

    private CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(customer.getCustomerId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setFullName(customer.getFullName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setAddress(customer.getAddress());
        response.setRole(customer.getRole().name());
        response.setCreatedAt(customer.getCreatedAt());
        return response;
    }
}
