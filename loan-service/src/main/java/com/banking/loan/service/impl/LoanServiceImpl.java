package com.banking.loan.service.impl;

import com.banking.common.exception.BadRequestException;
import com.banking.common.exception.ResourceNotFoundException;
import com.banking.loan.client.AccountServiceClient;
import com.banking.loan.dto.EmiCalculatorRequest;
import com.banking.loan.dto.LoanApplicationRequest;
import com.banking.loan.entity.Loan;
import com.banking.loan.enums.LoanStatus;
import com.banking.loan.repository.LoanRepository;
import com.banking.loan.service.LoanService;
import com.banking.loan.util.EmiCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final EmiCalculator emiCalculator;
    private final AccountServiceClient accountServiceClient;

    @Override
    @Transactional
    public Loan applyForLoan(Long userId, LoanApplicationRequest request) {
        // Verify account exists via Feign
        var accountResponse = accountServiceClient.getAccount(request.getAccountNumber());
        if (!accountResponse.isSuccess()) {
            throw new BadRequestException("Invalid account number provided for loan disbursement");
        }

        // Set interest rate based on loan type (hardcoded for now as per enterprise standards)
        BigDecimal interestRate = switch (request.getLoanType()) {
            case PERSONAL -> new BigDecimal("12.5");
            case HOME -> new BigDecimal("8.5");
            case VEHICLE -> new BigDecimal("9.0");
            case EDUCATION -> new BigDecimal("7.5");
            default -> new BigDecimal("10.0");
        };

        BigDecimal emi = emiCalculator.calculateEmi(request.getPrincipalAmount(), interestRate, request.getTenureMonths());
        BigDecimal totalPayable = emiCalculator.calculateTotalPayable(emi, request.getTenureMonths());

        Loan loan = Loan.builder()
                .loanNumber("LOAN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .userId(userId)
                .accountNumber(request.getAccountNumber())
                .loanType(request.getLoanType())
                .principalAmount(request.getPrincipalAmount())
                .interestRate(interestRate)
                .tenureMonths(request.getTenureMonths())
                .emiAmount(emi)
                .totalPayable(totalPayable)
                .outstandingAmount(totalPayable)
                .purpose(request.getPurpose())
                .status(LoanStatus.PENDING)
                .build();

        return loanRepository.save(loan);
    }

    @Override
    public List<Loan> getUserLoans(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    @Override
    public Loan getLoanByNumber(String loanNumber) {
        return loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with number: " + loanNumber));
    }

    @Override
    public Map<String, Object> calculateEmi(EmiCalculatorRequest request) {
        BigDecimal emi = emiCalculator.calculateEmi(request.getPrincipalAmount(), request.getAnnualInterestRate(), request.getTenureMonths());
        BigDecimal totalPayable = emiCalculator.calculateTotalPayable(emi, request.getTenureMonths());
        BigDecimal totalInterest = emiCalculator.calculateTotalInterest(totalPayable, request.getPrincipalAmount());

        Map<String, Object> result = new HashMap<>();
        result.put("monthlyEmi", emi);
        result.put("totalInterest", totalInterest);
        result.put("totalPayable", totalPayable);
        return result;
    }

    @Override
    @Transactional
    public Loan approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BadRequestException("Only pending loans can be approved");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setDisbursedAt(LocalDateTime.now());
        loan.setNextEmiDate(LocalDate.now().plusMonths(1));
        // In a real system, we'd trigger a transfer here to the account_number
        
        return loanRepository.save(loan);
    }

    @Override
    @Transactional
    public Loan rejectLoan(Long loanId, String reason) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        
        loan.setStatus(LoanStatus.REJECTED);
        return loanRepository.save(loan);
    }
}
