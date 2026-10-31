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

    private Long extractUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isBlank()) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException ignored) {}
        }
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Long) {
            return (Long) auth.getDetails();
        }
        throw new com.banking.common.exception.BadRequestException("Missing or invalid user identification header");
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<Loan>> apply(HttpServletRequest request, @Valid @RequestBody LoanApplicationRequest body) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success(loanService.applyForLoan(userId, body), "Loan application submitted"));
    }

    @GetMapping("/my-loans")
    public ResponseEntity<ApiResponse<List<Loan>>> getMyLoans(HttpServletRequest request) {
        Long userId = extractUserId(request);
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

    @PatchMapping("/{loanId}/approve")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Loan>> approveLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(loanService.approveLoan(loanId), "Loan approved"));
    }

    @PatchMapping("/{loanId}/reject")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Loan>> rejectLoan(
            @PathVariable Long loanId,
            @RequestParam(defaultValue = "Application does not meet policy criteria") String reason) {
        return ResponseEntity.ok(ApiResponse.success(loanService.rejectLoan(loanId, reason), "Loan rejected"));
    }
}
