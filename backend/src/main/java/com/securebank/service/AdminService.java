package com.securebank.service;

import com.securebank.dto.response.AccountResponse;
import com.securebank.dto.response.BankStatisticsResponse;
import com.securebank.dto.response.CustomerResponse;
import com.securebank.dto.response.TransactionResponse;

import java.util.List;

public interface AdminService {
    BankStatisticsResponse getBankStatistics();
    List<CustomerResponse> getAllCustomers();
    List<AccountResponse> getAllAccounts();
    List<TransactionResponse> getAllTransactions();
    AccountResponse blockAccount(String accountNumber);
    AccountResponse unblockAccount(String accountNumber);
}
