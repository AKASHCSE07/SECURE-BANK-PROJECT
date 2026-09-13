package com.securebank.service.impl;

import com.securebank.dto.request.AddBeneficiaryRequest;
import com.securebank.dto.response.BeneficiaryResponse;
import com.securebank.entity.Beneficiary;
import com.securebank.entity.Customer;
import com.securebank.exception.BankingException;
import com.securebank.exception.CustomerNotFoundException;
import com.securebank.exception.DuplicateResourceException;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.BeneficiaryRepository;
import com.securebank.repository.CustomerRepository;
import com.securebank.service.BeneficiaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public BeneficiaryServiceImpl(BeneficiaryRepository beneficiaryRepository, 
                                  CustomerRepository customerRepository, 
                                  AccountRepository accountRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public BeneficiaryResponse addBeneficiary(AddBeneficiaryRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(String.valueOf(request.getCustomerId())));

        if (beneficiaryRepository.existsByCustomerCustomerIdAndAccountNumber(
                request.getCustomerId(), request.getAccountNumber())) {
            throw new DuplicateResourceException("Beneficiary", request.getAccountNumber());
        }

        // Verify account exists in bank
        if (!accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new BankingException("Beneficiary account number does not exist in SecureBank system: " + request.getAccountNumber(), "ACCOUNT_NOT_FOUND");
        }

        Beneficiary beneficiary = new Beneficiary(
                customer,
                request.getBeneficiaryName(),
                request.getAccountNumber(),
                request.getBankName(),
                request.getEmail()
        );
        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getBeneficiariesByCustomerId(Long customerId) {
        return beneficiaryRepository.findByCustomerCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBeneficiary(Long customerId, Long beneficiaryId) {
        Beneficiary beneficiary = beneficiaryRepository.findByCustomerCustomerIdAndBeneficiaryId(customerId, beneficiaryId)
                .orElseThrow(() -> new BankingException("Beneficiary not found for this customer.", "BENEFICIARY_NOT_FOUND"));
        beneficiaryRepository.delete(beneficiary);
    }

    private BeneficiaryResponse mapToResponse(Beneficiary b) {
        BeneficiaryResponse res = new BeneficiaryResponse();
        res.setBeneficiaryId(b.getBeneficiaryId());
        res.setCustomerId(b.getCustomer().getCustomerId());
        res.setBeneficiaryName(b.getBeneficiaryName());
        res.setAccountNumber(b.getAccountNumber());
        res.setBankName(b.getBankName());
        res.setEmail(b.getEmail());
        res.setAddedAt(b.getAddedAt());
        return res;
    }
}
