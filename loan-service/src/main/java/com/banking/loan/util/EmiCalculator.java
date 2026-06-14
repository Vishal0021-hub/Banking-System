package com.banking.loan.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * EMI Calculator using the reducing balance (flat diminishing) method.
 * Formula: EMI = P * R * (1+R)^N / ((1+R)^N - 1)
 * where P = principal, R = monthly interest rate, N = tenure in months
 */
@Component
public class EmiCalculator {

    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        if (annualRatePercent.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(new BigDecimal(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        // Monthly rate
        BigDecimal monthlyRate = annualRatePercent
                .divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);

        // (1 + R)^N
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, new MathContext(10));

        // EMI = P * R * (1+R)^N / ((1+R)^N - 1)
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowN);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalPayable(BigDecimal emi, int tenureMonths) {
        return emi.multiply(new BigDecimal(tenureMonths)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalInterest(BigDecimal totalPayable, BigDecimal principal) {
        return totalPayable.subtract(principal).setScale(2, RoundingMode.HALF_UP);
    }
}
