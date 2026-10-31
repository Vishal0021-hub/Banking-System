package com.banking.user.controller;

import com.banking.common.dto.ApiResponse;
import com.banking.user.dto.UpdateProfileRequest;
import com.banking.user.dto.UserProfileDto;
import com.banking.user.enums.KycStatus;
import com.banking.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(HttpServletRequest request) {
        Long userId = extractUserId(request);
        String email = request.getHeader("X-User-Email");
        String name = request.getHeader("X-User-Name");
        return ResponseEntity.ok(ApiResponse.success(userService.getProfileOrAutoCreate(userId, email, name)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateProfileRequest body) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(userId, body), "Profile updated"));
    }

    @PostMapping("/internal/create")
    public ResponseEntity<ApiResponse<UserProfileDto>> createProfile(
            @RequestParam Long authUserId,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) String panNumber,
            @RequestParam(required = false) String aadharNumber) {
        UserProfileDto dto = userService.createProfile(authUserId, email, fullName, phone, panNumber, aadharNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "Profile created"));
    }

    @GetMapping("/internal/id/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getByIdInternal(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userId)));
    }

    @GetMapping("/internal/{email}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfileByEmail(email)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @PatchMapping("/{userId}/kyc")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateKyc(
            @PathVariable Long userId,
            @RequestParam KycStatus status) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateKycStatus(userId, status), "KYC status updated"));
    }

    @PatchMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDto>> toggleLock(
            @PathVariable Long userId,
            @RequestParam boolean lock) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.toggleAccountLock(userId, lock),
                lock ? "Account locked" : "Account unlocked"));
    }
}
