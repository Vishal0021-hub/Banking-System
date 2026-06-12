package com.banking.loan.controller;

import com.banking.common.dto.ApiResponse;
import com.banking.loan.dto.EmiCalculatorRequest;
import com.banking.loan.dto.LoanApplicationRequest;
import com.banking.loan.entity.Loan;
import com.banking.loan.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<Loan>> apply(HttpServletRequest request, @Valid @RequestBody LoanApplicationRequest body) {
        Long userId = Long.parseLong(request.getHeader("X-User-Id"));
        return ResponseEntity.ok(ApiResponse.success(loanService.applyForLoan(userId, body), "Loan application submitted"));
    }

    @GetMapping("/my-loans")
    public ResponseEntity<ApiResponse<List<Loan>>> getMyLoans(HttpServletRequest request) {
        Long userId = Long.parseLong(request.getHeader("X-User-Id"));
        return ResponseEntity.ok(ApiResponse.success(loanService.getUserLoans(userId)));
    }

    @PostMapping("/calculate-emi")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculate(@Valid @RequestBody EmiCalculatorRequest request) {
        return ResponseEntity.ok(ApiResponse.success(loanService.calculateEmi(request)));
    }

    @GetMapping("/{loanNumber}")
    public ResponseEntity<ApiResponse<Loan>> getLoan(@PathVariable String loanNumber) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getLoanByNumber(loanNumber)));
    }
}
