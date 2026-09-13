package com.securebank.repository;

import com.securebank.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    /**
     * Retrieves all transactions where the given account is either the source or destination.
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.accountId = :accountId OR t.destinationAccount.accountId = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.accountNumber = :accNo OR t.destinationAccount.accountNumber = :accNo ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountNumberPaged(@Param("accNo") String accNo, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.accountNumber = :accNo OR t.destinationAccount.accountNumber = :accNo) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountNumberAndDateRange(@Param("accNo") String accNo, 
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
}
