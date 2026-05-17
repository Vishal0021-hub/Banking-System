package com.banking.admin.client;

import com.banking.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", path = "/api/v1/users")
public interface UserServiceClient {
    @GetMapping
    ApiResponse<List<Map<String, Object>>> getAllUsers();

    @PatchMapping("/{userId}/kyc")
    ApiResponse<Map<String, Object>> updateKyc(@PathVariable Long userId, @RequestParam String status);

    @PatchMapping("/{userId}/lock")
    ApiResponse<Map<String, Object>> toggleLock(@PathVariable Long userId, @RequestParam boolean lock);
}
