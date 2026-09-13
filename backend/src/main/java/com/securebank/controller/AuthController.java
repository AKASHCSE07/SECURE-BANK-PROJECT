package com.securebank.controller;

import com.securebank.dto.request.CustomerRegisterRequest;
import com.securebank.dto.request.LoginRequest;
import com.securebank.dto.response.ApiResponse;
import com.securebank.dto.response.AuthResponse;
import com.securebank.dto.response.CustomerResponse;
import com.securebank.service.AuthService;
import com.securebank.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CustomerService customerService;

    public AuthController(AuthService authService, CustomerService customerService) {
        this.authService = authService;
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerResponse>> register(@Valid @RequestBody CustomerRegisterRequest request) {
        CustomerResponse response = customerService.registerCustomer(request);
        return new ResponseEntity<>(ApiResponse.ok("Registration successful. Please log in.", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", response));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<AuthResponse>> adminLogin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.adminLogin(request);
        return ResponseEntity.ok(ApiResponse.ok("Admin authentication granted.", response));
    }
}
