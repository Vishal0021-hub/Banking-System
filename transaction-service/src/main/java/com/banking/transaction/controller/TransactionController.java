package com.banking.transaction.controller;

import com.banking.common.dto.ApiResponse;
import com.banking.transaction.dto.*;
import com.banking.transaction.entity.AuditLog;
import com.banking.transaction.service.PdfStatementService;
import com.banking.transaction.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final PdfStatementService pdfStatementService;

    @PostMapping("/transactions/deposit")
    public ResponseEntity<ApiResponse<TransactionDto>> deposit(
            HttpServletRequest req, @Valid @RequestBody DepositRequest request) {
        Long userId = Long.parseLong(req.getHeader("X-User-Id"));
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.deposit(request, userId, req.getRemoteAddr()), "Deposit successful"));
    }

    @PostMapping("/transactions/withdraw")
    public ResponseEntity<ApiResponse<TransactionDto>> withdraw(
            HttpServletRequest req, @Valid @RequestBody WithdrawRequest request) {
        Long userId = Long.parseLong(req.getHeader("X-User-Id"));
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.withdraw(request, userId, req.getRemoteAddr()), "Withdrawal successful"));
    }

    @PostMapping("/transactions/transfer")
    public ResponseEntity<ApiResponse<TransactionDto>> transfer(
            HttpServletRequest req, @Valid @RequestBody TransferRequest request) {
        Long userId = Long.parseLong(req.getHeader("X-User-Id"));
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.transfer(request, userId, req.getRemoteAddr()), "Transfer initiated"));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionDto>>> getMyTransactions(
            HttpServletRequest req,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = Long.parseLong(req.getHeader("X-User-Id"));
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(transactionService.getUserTransactions(userId, pageable)));
    }

    @GetMapping("/transactions/account/{accountNumber}")
    public ResponseEntity<ApiResponse<Page<TransactionDto>>> getAccountTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.getAccountTransactions(accountNumber, pageable)));
    }

    @GetMapping("/transactions/{ref}")
    public ResponseEntity<ApiResponse<TransactionDto>> getTransaction(@PathVariable String ref) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getTransactionByRef(ref)));
    }

    @GetMapping("/transactions/flagged")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getFlaggedTransactions() {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getFlaggedTransactions()));
    }

    @GetMapping("/transactions/audit")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getMyAuditLogs(HttpServletRequest req) {
        Long userId = Long.parseLong(req.getHeader("X-User-Id"));
        return ResponseEntity.ok(ApiResponse.success(transactionService.getAuditLogs(userId)));
    }

    @GetMapping("/transactions/audit/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAllAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getAllAuditLogs()));
    }

    @GetMapping("/statements/{accountNumber}/download")
    public ResponseEntity<byte[]> downloadStatement(
            @PathVariable String accountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest req) {

        // Long userId = Long.parseLong(req.getHeader("X-User-Id"));
        String holderName = req.getHeader("X-User-Email");

        if (from == null)
            from = LocalDate.now().withDayOfMonth(1);
        if (to == null)
            to = LocalDate.now();

        Pageable pageable = PageRequest.of(0, 500);
        List<TransactionDto> transactions = transactionService
                .getAccountTransactions(accountNumber, pageable).getContent();

        byte[] pdf = pdfStatementService.generateStatement(
                accountNumber, holderName,
                from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                transactions);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"statement_" + accountNumber + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
