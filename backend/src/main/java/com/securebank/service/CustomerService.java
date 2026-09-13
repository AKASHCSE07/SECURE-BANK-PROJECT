package com.securebank.service;

import com.securebank.dto.request.CustomerRegisterRequest;
import com.securebank.dto.response.CustomerResponse;

public interface CustomerService {
    CustomerResponse registerCustomer(CustomerRegisterRequest request);
    CustomerResponse getCustomerById(Long customerId);
    CustomerResponse getCustomerByEmail(String email);
    boolean verifyPin(Long customerId, String plainPin);
}
