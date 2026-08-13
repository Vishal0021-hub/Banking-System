package com.banking.notification.client;

import com.banking.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "user-service", path = "/api/v1/users")
public interface UserClient {

    @GetMapping("/me")
    ApiResponse<Map<String, Object>> getProfile(@RequestHeader("X-User-Id") String userId);
}
