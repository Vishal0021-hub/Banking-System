package com.banking.loan.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmiCalculatorRequest {

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00")
    private BigDecimal principalAmount;

    @NotNull(message = "Annual interest rate is required")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "50.00")
    private BigDecimal annualInterestRate;

    @NotNull(message = "Tenure is required")
    @Min(1)
    @Max(360)
    private Integer tenureMonths;
}
