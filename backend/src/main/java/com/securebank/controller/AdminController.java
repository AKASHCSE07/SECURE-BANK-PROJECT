package com.securebank.controller;

import com.securebank.dto.response.*;
import com.securebank.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<BankStatisticsResponse>> getStatistics() {
        BankStatisticsResponse response = adminService.getBankStatistics();
        return ResponseEntity.ok(ApiResponse.ok("System statistics retrieved.", response));
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        List<CustomerResponse> response = adminService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.ok("All registered customers retrieved.", response));
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        List<AccountResponse> response = adminService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.ok("All bank accounts retrieved.", response));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        List<TransactionResponse> response = adminService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.ok("Global transaction audit stream retrieved.", response));
    }

    @PatchMapping("/accounts/{accountNumber}/block")
    public ResponseEntity<ApiResponse<AccountResponse>> blockAccount(@PathVariable("accountNumber") String accountNumber) {
        AccountResponse response = adminService.blockAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.ok("Account " + accountNumber + " has been BLOCKED.", response));
    }

    @PatchMapping("/accounts/{accountNumber}/unblock")
    public ResponseEntity<ApiResponse<AccountResponse>> unblockAccount(@PathVariable("accountNumber") String accountNumber) {
        AccountResponse response = adminService.unblockAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.ok("Account " + accountNumber + " has been UNBLOCKED.", response));
    }
}
