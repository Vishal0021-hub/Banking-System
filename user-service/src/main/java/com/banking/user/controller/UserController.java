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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(HttpServletRequest request) {
        Long userId = Long.parseLong(request.getHeader("X-User-Id"));
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateProfileRequest body) {
        Long userId = Long.parseLong(request.getHeader("X-User-Id"));
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(userId, body), "Profile updated"));
    }

    @PostMapping("/internal/create")
    public ResponseEntity<ApiResponse<UserProfileDto>> createProfile(
            @RequestParam Long authUserId,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam String phone) {
        UserProfileDto dto = userService.createProfile(authUserId, email, fullName, phone);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "Profile created"));
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
