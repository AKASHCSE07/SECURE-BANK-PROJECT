package com.securebank.repository;

import com.securebank.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByCustomerCustomerId(Long customerId);
    boolean existsByCustomerCustomerIdAndAccountNumber(Long customerId, String accountNumber);
    Optional<Beneficiary> findByCustomerCustomerIdAndBeneficiaryId(Long customerId, Long beneficiaryId);
}
