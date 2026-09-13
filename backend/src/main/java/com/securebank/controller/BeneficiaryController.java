package com.securebank.controller;

import com.securebank.dto.request.AddBeneficiaryRequest;
import com.securebank.dto.response.ApiResponse;
import com.securebank.dto.response.BeneficiaryResponse;
import com.securebank.service.BeneficiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> addBeneficiary(@Valid @RequestBody AddBeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.addBeneficiary(request);
        return new ResponseEntity<>(ApiResponse.ok("Beneficiary added successfully.", response), HttpStatus.CREATED);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getBeneficiaries(@PathVariable("customerId") Long customerId) {
        List<BeneficiaryResponse> response = beneficiaryService.getBeneficiariesByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.ok("Beneficiaries retrieved.", response));
    }

    @DeleteMapping("/customer/{customerId}/{beneficiaryId}")
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(
            @PathVariable("customerId") Long customerId,
            @PathVariable("beneficiaryId") Long beneficiaryId) {
        beneficiaryService.deleteBeneficiary(customerId, beneficiaryId);
        return ResponseEntity.ok(ApiResponse.ok("Beneficiary removed successfully.", null));
    }
}
