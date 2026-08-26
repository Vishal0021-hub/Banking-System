package com.banking.transaction.service;

import com.banking.transaction.entity.Transaction;
import com.banking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Rule-based fraud detection service.
 * Flags transactions based on velocity, amount, and behavioral patterns.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;

    // Fraud thresholds
    private static final BigDecimal LARGE_AMOUNT_THRESHOLD = new BigDecimal("100000");
    private static final BigDecimal NEW_BENEFICIARY_THRESHOLD = new BigDecimal("50000");
    private static final int MAX_TRANSACTIONS_PER_10_MIN = 5;
    private static final int MAX_TRANSACTIONS_PER_DAY = 20;

    public record FraudResult(boolean flagged, String reason) {
    };

    public FraudResult analyze(String fromAccountNumber, BigDecimal amount,
            String toAccountNumber, Long userId) {
        // Rule 1: Large single transaction
        if (amount.compareTo(LARGE_AMOUNT_THRESHOLD) > 0) {
            log.warn("FRAUD ALERT: Large transaction ₹{} from {}", amount, fromAccountNumber);
            return new FraudResult(true,
                    "Transaction amount ₹" + amount + " exceeds single-transaction limit of ₹1,00,000");
        }

        // Rule 2: Velocity — too many transactions in 10 minutes
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        long recentCount = transactionRepository.countTransactionsSince(fromAccountNumber, tenMinutesAgo);
        if (recentCount >= MAX_TRANSACTIONS_PER_10_MIN) {
            log.warn("FRAUD ALERT: Velocity check failed for account {}", fromAccountNumber);
            return new FraudResult(true,
                    "Too many transactions (" + recentCount + ") in the last 10 minutes");
        }

        // Rule 3: Daily transaction count limit
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long dailyCount = transactionRepository.countTransactionsSince(fromAccountNumber, startOfDay);
        if (dailyCount >= MAX_TRANSACTIONS_PER_DAY) {
            return new FraudResult(true,
                    "Daily transaction limit of " + MAX_TRANSACTIONS_PER_DAY + " reached");
        }

        // Rule 4: High amount to a new beneficiary within 24 hours
        if (amount.compareTo(NEW_BENEFICIARY_THRESHOLD) > 0) {
            LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
            List<Transaction> previousTransactions = transactionRepository
                    .findTransfersSince(fromAccountNumber, yesterday);
            boolean isNewBeneficiary = previousTransactions.stream()
                    .noneMatch(t -> toAccountNumber.equals(t.getToAccountNumber()));
            if (isNewBeneficiary) {
                log.warn("FRAUD ALERT: Large transfer to new beneficiary from {}", fromAccountNumber);
                return new FraudResult(true,
                        "High-value transfer ₹" + amount + " to a new beneficiary within 24 hours");
            }
        }

        return new FraudResult(false, null);
    }
}
