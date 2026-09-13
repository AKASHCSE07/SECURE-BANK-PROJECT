package com.securebank.controller;

import com.securebank.dto.request.DepositRequest;
import com.securebank.dto.request.TransferRequest;
import com.securebank.dto.request.WithdrawRequest;
import com.securebank.dto.response.ApiResponse;
import com.securebank.dto.response.TransactionResponse;
import com.securebank.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.deposit(request);
        return ResponseEntity.ok(ApiResponse.ok("Deposit completed successfully.", response));
    }

    @PostMapping("/transactions/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(@Valid @RequestBody WithdrawRequest request) {
        TransactionResponse response = transactionService.withdraw(request);
        return ResponseEntity.ok(ApiResponse.ok("Withdrawal approved and processed.", response));
    }

    @PostMapping("/transfers")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(request);
        return ResponseEntity.ok(ApiResponse.ok("Fund transfer completed successfully.", response));
    }

    @GetMapping("/transactions/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAccountTransactions(
            @PathVariable("accountNumber") String accountNumber) {
        List<TransactionResponse> response = transactionService.getAccountTransactions(accountNumber);
        return ResponseEntity.ok(ApiResponse.ok("Transaction history retrieved.", response));
    }

    @GetMapping("/transactions/account/{accountNumber}/filter")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> filterTransactions(
            @PathVariable("accountNumber") String accountNumber,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<TransactionResponse> response = transactionService.filterTransactions(accountNumber, start, end);
        return ResponseEntity.ok(ApiResponse.ok("Filtered transactions retrieved.", response));
    }
}
