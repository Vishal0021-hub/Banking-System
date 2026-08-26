package com.banking.transaction.repository;

import com.banking.transaction.entity.Transaction;
import com.banking.transaction.enums.TransactionStatus;
import com.banking.transaction.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
       Optional<Transaction> findByReferenceNumber(String referenceNumber);

       Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

       @Query("SELECT t FROM Transaction t WHERE " +
                     "(t.fromAccountNumber = :accountNumber OR t.toAccountNumber = :accountNumber) " +
                     "ORDER BY t.createdAt DESC")
       Page<Transaction> findByAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

       @Query("SELECT t FROM Transaction t WHERE t.fromAccountNumber = :accountNumber " +
                     "AND t.transactionType = 'TRANSFER' " +
                     "AND t.createdAt >= :since AND t.status = 'SUCCESS'")
       List<Transaction> findTransfersSince(@Param("accountNumber") String accountNumber,
                     @Param("since") LocalDateTime since);

       @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromAccountNumber = :accountNumber " +
                     "AND t.createdAt >= :since")
       long countTransactionsSince(@Param("accountNumber") String accountNumber,
                     @Param("since") LocalDateTime since);

       @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromAccountNumber = :accountNumber " +
                     "AND t.transactionType = 'TRANSFER' AND t.status = 'SUCCESS' " +
                     "AND t.createdAt >= :since")
       BigDecimal sumTransferAmountSince(@Param("accountNumber") String accountNumber,
                     @Param("since") LocalDateTime since);

       List<Transaction> findByFraudFlaggedTrue();

       @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
       List<Transaction> findBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

       List<Transaction> findByUserIdAndTransactionTypeAndCreatedAtBetween(
                     Long userId, TransactionType type, LocalDateTime start, LocalDateTime end);
}
