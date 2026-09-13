package com.securebank.service;

import com.securebank.dto.request.DepositRequest;
import com.securebank.dto.request.TransferRequest;
import com.securebank.dto.request.WithdrawRequest;
import com.securebank.dto.response.TransactionResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    TransactionResponse deposit(DepositRequest request);
    TransactionResponse withdraw(WithdrawRequest request);
    TransactionResponse transfer(TransferRequest request);
    List<TransactionResponse> getAccountTransactions(String accountNumber);
    List<TransactionResponse> filterTransactions(String accountNumber, LocalDateTime start, LocalDateTime end);
}
