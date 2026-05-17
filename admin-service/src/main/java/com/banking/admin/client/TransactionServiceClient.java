package com.banking.admin.client;

import com.banking.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "transaction-service", path = "/api/v1/transactions")
public interface TransactionServiceClient {
    @GetMapping("/flagged")
    ApiResponse<List<Map<String, Object>>> getFlaggedTransactions();

    @GetMapping("/audit/all")
    ApiResponse<List<Map<String, Object>>> getAllAuditLogs();
}
