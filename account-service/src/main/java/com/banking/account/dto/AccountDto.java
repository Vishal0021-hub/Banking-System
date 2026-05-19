package com.banking.account.dto;

import com.banking.account.enums.AccountStatus;
import com.banking.account.enums.AccountType;
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
public class AccountDto {
    private Long id;
    private String accountNumber;
    private Long userId;
    private String userEmail;
    private String accountHolderName;
    private AccountType accountType;
    private AccountStatus status;
    private BigDecimal balance;
    private String currency;
    private String ifscCode;
    private String branchName;
    private BigDecimal dailyTransferLimit;
    private BigDecimal transferredToday;
    private LocalDateTime createdAt;
}
