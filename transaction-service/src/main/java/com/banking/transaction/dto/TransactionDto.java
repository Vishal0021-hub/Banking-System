package com.banking.transaction.dto;

import com.banking.transaction.enums.TransactionStatus;
import com.banking.transaction.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private String referenceNumber;
    private Long userId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String description;
    private boolean fraudFlagged;
    private String fraudReason;
    private String currency;
    private String channel;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
