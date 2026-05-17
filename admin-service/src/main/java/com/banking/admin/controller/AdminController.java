package com.banking.admin.controller;

import com.banking.admin.client.AccountServiceClient;
import com.banking.admin.client.TransactionServiceClient;
import com.banking.admin.client.UserServiceClient;
import com.banking.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserServiceClient userServiceClient;
    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;

    // User Management
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUsers() {
        return ResponseEntity.ok(userServiceClient.getAllUsers());
    }

    @PatchMapping("/users/{userId}/kyc")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approveKyc(@PathVariable Long userId, @RequestParam String status) {
        return ResponseEntity.ok(userServiceClient.updateKyc(userId, status));
    }

    @PatchMapping("/users/{userId}/lock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> lockUser(@PathVariable Long userId, @RequestParam boolean lock) {
        return ResponseEntity.ok(userServiceClient.toggleLock(userId, lock));
    }

    // Account Management
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAccounts() {
        return ResponseEntity.ok(accountServiceClient.getAllAccounts());
    }

    @PatchMapping("/accounts/{id}/freeze")
    public ResponseEntity<ApiResponse<Map<String, Object>>> freezeAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountServiceClient.freeze(id));
    }

    // Monitoring
    @GetMapping("/transactions/flagged")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFlagged() {
        return ResponseEntity.ok(transactionServiceClient.getFlaggedTransactions());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAudits() {
        return ResponseEntity.ok(transactionServiceClient.getAllAuditLogs());
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        // Mocking aggregate stats for now as an enterprise dashboard endpoint
        Map<String, Object> stats = Map.of(
            "totalUsers", 1500,
            "activeAccounts", 1200,
            "flaggedToday", 5,
            "pendingLoans", 12
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
