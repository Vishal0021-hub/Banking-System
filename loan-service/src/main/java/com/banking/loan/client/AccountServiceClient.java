package com.banking.loan.client;

import com.banking.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "account-service", path = "/api/v1/accounts/internal")
public interface AccountServiceClient {

    @GetMapping("/{accountNumber}")
    ApiResponse<Map<String, Object>> getAccount(@PathVariable String accountNumber);
}
