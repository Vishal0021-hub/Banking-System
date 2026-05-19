package com.banking.account.entity;

import com.banking.account.enums.AccountStatus;
import com.banking.account.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_number", columnList = "account_number", unique = true),
    @Index(name = "idx_account_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 16)
    private String accountNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "account_holder_name", nullable = false)
    private String accountHolderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "ifsc_code", nullable = false, length = 11)
    @Builder.Default
    private String ifscCode = "BANK0001234";

    @Column(name = "branch_name", nullable = false)
    @Builder.Default
    private String branchName = "Main Branch";

    @Column(name = "daily_transfer_limit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyTransferLimit = new BigDecimal("100000.00");

    @Column(name = "transferred_today", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal transferredToday = BigDecimal.ZERO;

    @Column(name = "daily_limit_reset_date")
    private LocalDateTime dailyLimitResetDate;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
