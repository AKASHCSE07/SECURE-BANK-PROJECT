package com.securebank.service;

import com.securebank.dto.request.AddBeneficiaryRequest;
import com.securebank.dto.response.BeneficiaryResponse;

import java.util.List;

public interface BeneficiaryService {
    BeneficiaryResponse addBeneficiary(AddBeneficiaryRequest request);
    List<BeneficiaryResponse> getBeneficiariesByCustomerId(Long customerId);
    void deleteBeneficiary(Long customerId, Long beneficiaryId);
}
