package com.banking.loan.service;

import com.banking.loan.dto.EmiCalculatorRequest;
import com.banking.loan.dto.LoanApplicationRequest;
import com.banking.loan.entity.Loan;

import java.util.List;
import java.util.Map;

public interface LoanService {
    Loan applyForLoan(Long userId, LoanApplicationRequest request);
    List<Loan> getUserLoans(Long userId);
    Loan getLoanByNumber(String loanNumber);
    Map<String, Object> calculateEmi(EmiCalculatorRequest request);
    Loan approveLoan(Long loanId);
    Loan rejectLoan(Long loanId, String reason);
}
