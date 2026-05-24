package com.banking.transaction.service;

import com.banking.transaction.dto.*;
import com.banking.transaction.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {
    TransactionDto deposit(DepositRequest request, Long userId, String ipAddress);
    TransactionDto withdraw(WithdrawRequest request, Long userId, String ipAddress);
    TransactionDto transfer(TransferRequest request, Long userId, String ipAddress);
    Page<TransactionDto> getUserTransactions(Long userId, Pageable pageable);
    Page<TransactionDto> getAccountTransactions(String accountNumber, Pageable pageable);
    TransactionDto getTransactionByRef(String ref);
    List<TransactionDto> getFlaggedTransactions();
    List<AuditLog> getAuditLogs(Long userId);
    List<AuditLog> getAllAuditLogs();
}
