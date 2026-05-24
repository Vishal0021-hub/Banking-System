package com.banking.transaction.entity;

import com.banking.transaction.enums.TransactionStatus;
import com.banking.transaction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_txn_from_account", columnList = "from_account_number"),
    @Index(name = "idx_txn_to_account", columnList = "to_account_number"),
    @Index(name = "idx_txn_user", columnList = "user_id"),
    @Index(name = "idx_txn_ref", columnList = "reference_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", nullable = false, unique = true, length = 24)
    private String referenceNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "from_account_number", length = 16)
    private String fromAccountNumber;

    @Column(name = "to_account_number", length = 16)
    private String toAccountNumber;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", precision = 18, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 255)
    private String description;

    @Column(name = "fraud_flagged", nullable = false)
    @Builder.Default
    private boolean fraudFlagged = false;

    @Column(name = "fraud_reason", length = 255)
    private String fraudReason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(length = 20)
    @Builder.Default
    private String channel = "NET_BANKING";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
