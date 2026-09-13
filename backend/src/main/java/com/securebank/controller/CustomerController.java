package com.securebank.controller;

import com.securebank.dto.request.CustomerRegisterRequest;
import com.securebank.dto.response.ApiResponse;
import com.securebank.dto.response.CustomerResponse;
import com.securebank.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerResponse>> registerCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        CustomerResponse response = customerService.registerCustomer(request);
        return new ResponseEntity<>(ApiResponse.ok("Customer registration completed successfully.", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable("id") Long customerId) {
        CustomerResponse response = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(ApiResponse.ok("Customer profile retrieved.", response));
    }

    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByEmail(@RequestParam("email") String email) {
        CustomerResponse response = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(ApiResponse.ok("Customer profile retrieved.", response));
    }
}
