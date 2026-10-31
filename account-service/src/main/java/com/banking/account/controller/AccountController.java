package com.banking.account.controller;

import com.banking.account.dto.AccountDto;
import com.banking.account.dto.AddBeneficiaryRequest;
import com.banking.account.dto.CreateAccountRequest;
import com.banking.account.entity.Beneficiary;
import com.banking.account.service.AccountService;
import com.banking.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

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

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<AccountDto>> createAccount(
            HttpServletRequest req,
            @Valid @RequestBody CreateAccountRequest request) {
        Long userId = extractUserId(req);
        String email = req.getHeader("X-User-Email");
        String name = req.getHeader("X-User-Name") != null ? req.getHeader("X-User-Name") : email;
        AccountDto dto = accountService.createAccount(userId, email, name, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "Account created"));
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountDto>>> getMyAccounts(HttpServletRequest req) {
        Long userId = extractUserId(req);
        return ResponseEntity.ok(ApiResponse.success(accountService.getUserAccounts(userId)));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountDto>> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountByNumber(accountNumber)));
    }

    @GetMapping("/accounts/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AccountDto>>> getAllAccounts() {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAllAccounts()));
    }

    @PatchMapping("/accounts/{id}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AccountDto>> freeze(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.freezeAccount(id), "Account frozen"));
    }

    @PatchMapping("/accounts/{id}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AccountDto>> unfreeze(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.unfreezeAccount(id), "Account unfrozen"));
    }

    // --- Beneficiary endpoints ---

    @PostMapping("/beneficiaries")
    public ResponseEntity<ApiResponse<Beneficiary>> addBeneficiary(
            HttpServletRequest req,
            @Valid @RequestBody AddBeneficiaryRequest request) {
        Long userId = extractUserId(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(accountService.addBeneficiary(userId, request), "Beneficiary added"));
    }

    @GetMapping("/beneficiaries")
    public ResponseEntity<ApiResponse<List<Beneficiary>>> getBeneficiaries(HttpServletRequest req) {
        Long userId = extractUserId(req);
        return ResponseEntity.ok(ApiResponse.success(accountService.getBeneficiaries(userId)));
    }

    @DeleteMapping("/beneficiaries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(
            HttpServletRequest req, @PathVariable Long id) {
        Long userId = extractUserId(req);
        accountService.deleteBeneficiary(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Beneficiary removed"));
    }

    // Internal endpoint for transaction-service
    @GetMapping("/accounts/internal/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountDto>> getAccountInternal(@PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountByNumber(accountNumber)));
    }

    @PutMapping("/accounts/internal/{accountNumber}/balance")
    public ResponseEntity<ApiResponse<AccountDto>> updateBalance(
            @PathVariable String accountNumber,
            @RequestParam java.math.BigDecimal balance) {
        return ResponseEntity.ok(ApiResponse.success(accountService.updateBalance(accountNumber, balance)));
    }
}
