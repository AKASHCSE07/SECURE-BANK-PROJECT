package com.securebank.service.impl;

import com.securebank.dto.request.LoginRequest;
import com.securebank.dto.response.AuthResponse;
import com.securebank.entity.Admin;
import com.securebank.entity.Customer;
import com.securebank.exception.UnauthorizedOperationException;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.AdminRepository;
import com.securebank.repository.CustomerRepository;
import com.securebank.security.JwtTokenProvider;
import com.securebank.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final AccountRepository accountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager, 
                           CustomerRepository customerRepository, 
                           AdminRepository adminRepository, 
                           AccountRepository accountRepository,
                           JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
        this.accountRepository = accountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtTokenProvider.generateToken(authentication);

            Customer customer = customerRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UnauthorizedOperationException("Invalid email or password."));

            AuthResponse authResponse = new AuthResponse(
                    token,
                    customer.getCustomerId(),
                    customer.getEmail(),
                    customer.getFullName(),
                    customer.getRole().name()
            );
            authResponse.setFirstName(customer.getFirstName());
            authResponse.setLastName(customer.getLastName());
            accountRepository.findByCustomerCustomerId(customer.getCustomerId()).stream()
                    .findFirst()
                    .ifPresent(acc -> authResponse.setAccountNumber(acc.getAccountNumber()));

            return authResponse;
        } catch (BadCredentialsException e) {
            throw new UnauthorizedOperationException("Invalid email or password.");
        }
    }

    @Override
    public AuthResponse adminLogin(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtTokenProvider.generateToken(authentication);

            Admin admin = adminRepository.findByUsername(request.getEmail())
                    .or(() -> adminRepository.findByEmail(request.getEmail()))
                    .orElseThrow(() -> new UnauthorizedOperationException("Invalid admin credentials."));

            return new AuthResponse(
                    token,
                    admin.getAdminId(),
                    admin.getEmail(),
                    admin.getUsername(),
                    admin.getRole().name()
            );
        } catch (BadCredentialsException e) {
            throw new UnauthorizedOperationException("Invalid admin credentials.");
        }
    }
}
