package com.banking.transaction.service.impl;

import com.banking.common.exception.BadRequestException;
import com.banking.common.exception.InsufficientFundsException;
import com.banking.common.exception.ResourceNotFoundException;
import com.banking.transaction.client.AccountServiceClient;
import com.banking.transaction.dto.*;
import com.banking.transaction.entity.AuditLog;
import com.banking.transaction.entity.Transaction;
import com.banking.transaction.enums.TransactionStatus;
import com.banking.transaction.enums.TransactionType;
import com.banking.transaction.kafka.TransactionEventProducer;
import com.banking.transaction.repository.AuditLogRepository;
import com.banking.transaction.repository.TransactionRepository;
import com.banking.transaction.service.FraudDetectionService;
import com.banking.transaction.service.TransactionService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final AccountServiceClient accountServiceClient;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionEventProducer eventProducer;

    @Override
    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "depositFallback")
    public TransactionDto deposit(DepositRequest request, Long userId, String ipAddress) {
        var accountResponse = accountServiceClient.getAccount(request.getAccountNumber());
        if (accountResponse == null || accountResponse.getData() == null) {
            throw new ResourceNotFoundException("Account", "accountNumber", request.getAccountNumber());
        }
        Map<String, Object> accountData = accountResponse.getData();
        validateAccountActive(accountData, request.getAccountNumber());

        BigDecimal currentBalance = new BigDecimal(accountData.get("balance").toString());
        BigDecimal newBalance = currentBalance.add(request.getAmount());

        accountServiceClient.updateBalance(request.getAccountNumber(), newBalance);

        Transaction txn = Transaction.builder()
                .referenceNumber(generateRef())
                .userId(userId)
                .toAccountNumber(request.getAccountNumber())
                .amount(request.getAmount())
                .balanceBefore(currentBalance)
                .balanceAfter(newBalance)
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Cash Deposit")
                .ipAddress(ipAddress)
                .completedAt(LocalDateTime.now())
                .build();

        txn = transactionRepository.save(txn);
        auditLog(userId, "DEPOSIT", "Transaction", txn.getId(), "Deposit ₹" + request.getAmount(), ipAddress);
        eventProducer.publishTransactionEvent(txn);

        return mapToDto(txn);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "withdrawFallback")
    public TransactionDto withdraw(WithdrawRequest request, Long userId, String ipAddress) {
        var accountResponse = accountServiceClient.getAccount(request.getAccountNumber());
        Map<String, Object> accountData = accountResponse.getData();
        validateAccountActive(accountData, request.getAccountNumber());

        BigDecimal currentBalance = new BigDecimal(accountData.get("balance").toString());
        if (currentBalance.compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient balance. Available: ₹" + currentBalance);
        }

        BigDecimal newBalance = currentBalance.subtract(request.getAmount());
        accountServiceClient.updateBalance(request.getAccountNumber(), newBalance);

        Transaction txn = Transaction.builder()
                .referenceNumber(generateRef())
                .userId(userId)
                .fromAccountNumber(request.getAccountNumber())
                .amount(request.getAmount())
                .balanceBefore(currentBalance)
                .balanceAfter(newBalance)
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Cash Withdrawal")
                .ipAddress(ipAddress)
                .completedAt(LocalDateTime.now())
                .build();

        txn = transactionRepository.save(txn);
        auditLog(userId, "WITHDRAWAL", "Transaction", txn.getId(), "Withdraw ₹" + request.getAmount(), ipAddress);
        eventProducer.publishTransactionEvent(txn);

        return mapToDto(txn);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "transferFallback")
    public TransactionDto transfer(TransferRequest request, Long userId, String ipAddress) {
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new BadRequestException("Cannot transfer to the same account");
        }

        var fromResponse = accountServiceClient.getAccount(request.getFromAccountNumber());
        var toResponse = accountServiceClient.getAccount(request.getToAccountNumber());

        Map<String, Object> fromData = fromResponse.getData();
        Map<String, Object> toData = toResponse.getData();

        validateAccountActive(fromData, request.getFromAccountNumber());
        validateAccountActive(toData, request.getToAccountNumber());

        BigDecimal fromBalance = new BigDecimal(fromData.get("balance").toString());
        BigDecimal toBalance = new BigDecimal(toData.get("balance").toString());

        if (fromBalance.compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient balance. Available: ₹" + fromBalance);
        }

        BigDecimal dailyLimit = new BigDecimal(fromData.get("dailyTransferLimit").toString());
        BigDecimal transferredToday = new BigDecimal(fromData.get("transferredToday").toString());
        
        if (transferredToday.add(request.getAmount()).compareTo(dailyLimit) > 0) {
            throw new BadRequestException("Daily transfer limit exceeded. Remaining limit: ₹" + dailyLimit.subtract(transferredToday));
        }

        // Fraud detection
        FraudDetectionService.FraudResult fraudResult = fraudDetectionService.analyze(
                request.getFromAccountNumber(), request.getAmount(),
                request.getToAccountNumber(), userId);

        TransactionStatus status = fraudResult.flagged() ? TransactionStatus.FLAGGED : TransactionStatus.SUCCESS;

        BigDecimal newFromBalance = fromBalance.subtract(request.getAmount());
        BigDecimal newToBalance = toBalance.add(request.getAmount());

        if (!fraudResult.flagged()) {
            accountServiceClient.updateBalance(request.getFromAccountNumber(), newFromBalance);
            accountServiceClient.updateBalance(request.getToAccountNumber(), newToBalance);
        }

        Transaction txn = Transaction.builder()
                .referenceNumber(generateRef())
                .userId(userId)
                .fromAccountNumber(request.getFromAccountNumber())
                .toAccountNumber(request.getToAccountNumber())
                .amount(request.getAmount())
                .balanceBefore(fromBalance)
                .balanceAfter(fraudResult.flagged() ? fromBalance : newFromBalance)
                .transactionType(TransactionType.TRANSFER)
                .status(status)
                .description(request.getDescription() != null ? request.getDescription() : "Fund Transfer")
                .fraudFlagged(fraudResult.flagged())
                .fraudReason(fraudResult.reason())
                .ipAddress(ipAddress)
                .completedAt(fraudResult.flagged() ? null : LocalDateTime.now())
                .build();

        txn = transactionRepository.save(txn);
        auditLog(userId, "TRANSFER", "Transaction", txn.getId(),
                "Transfer ₹" + request.getAmount() + " to " + request.getToAccountNumber(), ipAddress);
        eventProducer.publishTransactionEvent(txn);

        return mapToDto(txn);
    }

    @Override
    public Page<TransactionDto> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    @Override
    public Page<TransactionDto> getAccountTransactions(String accountNumber, Pageable pageable) {
        return transactionRepository.findByAccountNumber(accountNumber, pageable)
                .map(this::mapToDto);
    }

    @Override
    public TransactionDto getTransactionByRef(String ref) {
        return mapToDto(transactionRepository.findByReferenceNumber(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "referenceNumber", ref)));
    }

    @Override
    public List<TransactionDto> getFlaggedTransactions() {
        return transactionRepository.findByFraudFlaggedTrue().stream().map(this::mapToDto).toList();
    }

    @Override
    public List<AuditLog> getAuditLogs(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }

    private void validateAccountActive(Map<String, Object> data, String accountNumber) {
        String status = data.get("status").toString();
        if (!"ACTIVE".equals(status)) {
            throw new BadRequestException("Account " + accountNumber + " is not active (status: " + status + ")");
        }
    }

    private void auditLog(Long userId, String action, String entity, Long entityId,
                          String desc, String ip) {
        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entity)
                .entityId(entityId)
                .description(desc)
                .ipAddress(ip)
                .build());
    }

    private String generateRef() {
        return "TXN" + System.currentTimeMillis() +
               UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private TransactionDto mapToDto(Transaction t) {
        return TransactionDto.builder()
                .id(t.getId())
                .referenceNumber(t.getReferenceNumber())
                .userId(t.getUserId())
                .fromAccountNumber(t.getFromAccountNumber())
                .toAccountNumber(t.getToAccountNumber())
                .amount(t.getAmount())
                .balanceBefore(t.getBalanceBefore())
                .balanceAfter(t.getBalanceAfter())
                .transactionType(t.getTransactionType())
                .status(t.getStatus())
                .description(t.getDescription())
                .fraudFlagged(t.isFraudFlagged())
                .fraudReason(t.getFraudReason())
                .currency(t.getCurrency())
                .channel(t.getChannel())
                .createdAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build();
    }

    public TransactionDto depositFallback(DepositRequest request, Long userId, String ipAddress, Throwable t) {
        log.error("Circuit breaker 'accountService' triggered for deposit. Error: {}", t.getMessage());
        throw new BadRequestException("Account service is temporarily unavailable. Please try again later.");
    }

    public TransactionDto withdrawFallback(WithdrawRequest request, Long userId, String ipAddress, Throwable t) {
        log.error("Circuit breaker 'accountService' triggered for withdraw. Error: {}", t.getMessage());
        throw new BadRequestException("Account service is temporarily unavailable. Please try again later.");
    }

    public TransactionDto transferFallback(TransferRequest request, Long userId, String ipAddress, Throwable t) {
        log.error("Circuit breaker 'accountService' triggered for transfer. Error: {}", t.getMessage());
        throw new BadRequestException("Account service is temporarily unavailable. Please try again later.");
    }
}
