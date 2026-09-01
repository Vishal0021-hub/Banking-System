package com.banking.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferEvent {
    private String referenceNumber;
    private Long userId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String status; // REQUESTED, SUCCESS, FAILED
    private String reason;
}
