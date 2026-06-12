package com.banking.loan.repository;

import com.banking.loan.entity.Loan;
import com.banking.loan.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    Optional<Loan> findByLoanNumber(String loanNumber);
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByStatusAndNextEmiDateLessThanEqual(LoanStatus status, LocalDate date);
}
