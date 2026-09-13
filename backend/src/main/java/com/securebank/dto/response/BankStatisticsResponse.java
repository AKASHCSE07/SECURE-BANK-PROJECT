package com.securebank.dto.response;

import java.math.BigDecimal;

public class BankStatisticsResponse {
    private long totalCustomers;
    private long totalAccounts;
    private long activeAccounts;
    private long blockedAccounts;
    private BigDecimal totalVaultBalance;
    private long totalTransactionsCount;

    public BankStatisticsResponse() {}

    public BankStatisticsResponse(long totalCustomers, long totalAccounts, long activeAccounts, 
                                  long blockedAccounts, BigDecimal totalVaultBalance, long totalTransactionsCount) {
        this.totalCustomers = totalCustomers;
        this.totalAccounts = totalAccounts;
        this.activeAccounts = activeAccounts;
        this.blockedAccounts = blockedAccounts;
        this.totalVaultBalance = totalVaultBalance;
        this.totalTransactionsCount = totalTransactionsCount;
    }

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
    public long getTotalAccounts() { return totalAccounts; }
    public void setTotalAccounts(long totalAccounts) { this.totalAccounts = totalAccounts; }
    public long getActiveAccounts() { return activeAccounts; }
    public void setActiveAccounts(long activeAccounts) { this.activeAccounts = activeAccounts; }
    public long getBlockedAccounts() { return blockedAccounts; }
    public void setBlockedAccounts(long blockedAccounts) { this.blockedAccounts = blockedAccounts; }
    public BigDecimal getTotalVaultBalance() { return totalVaultBalance; }
    public void setTotalVaultBalance(BigDecimal totalVaultBalance) { this.totalVaultBalance = totalVaultBalance; }
    public long getTotalTransactionsCount() { return totalTransactionsCount; }
    public void setTotalTransactionsCount(long totalTransactionsCount) { this.totalTransactionsCount = totalTransactionsCount; }
}
