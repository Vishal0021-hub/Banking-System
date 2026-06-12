package com.banking.loan.dto;

import com.banking.loan.enums.LoanType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "10000.00", message = "Minimum loan amount is ₹10,000")
    @DecimalMax(value = "10000000.00", message = "Maximum loan amount is ₹1,00,00,000")
    private BigDecimal principalAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 3, message = "Minimum tenure is 3 months")
    @Max(value = 360, message = "Maximum tenure is 360 months (30 years)")
    private Integer tenureMonths;

    @Size(max = 512)
    private String purpose;
}
