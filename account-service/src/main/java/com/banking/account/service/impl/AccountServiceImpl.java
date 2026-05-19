package com.banking.account.service.impl;

import com.banking.account.dto.AccountDto;
import com.banking.account.dto.AddBeneficiaryRequest;
import com.banking.account.dto.CreateAccountRequest;
import com.banking.account.entity.Account;
import com.banking.account.entity.Beneficiary;
import com.banking.account.enums.AccountStatus;
import com.banking.account.repository.AccountRepository;
import com.banking.account.repository.BeneficiaryRepository;
import com.banking.account.service.AccountService;
import com.banking.account.util.AccountNumberGenerator;
import com.banking.common.exception.BadRequestException;
import com.banking.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    public AccountDto createAccount(Long userId, String userEmail, String holderName,
                                    CreateAccountRequest request) {
        // Max 5 accounts per user
        if (accountRepository.countByUserId(userId) >= 5) {
            throw new BadRequestException("Maximum account limit (5) reached");
        }

        String accountNumber;
        int attempts = 0;
        do {
            accountNumber = accountNumberGenerator.generate();
            attempts++;
            if (attempts > 10) throw new RuntimeException("Failed to generate unique account number");
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .userId(userId)
                .userEmail(userEmail)
                .accountHolderName(holderName)
                .accountType(request.getAccountType())
                .branchName(request.getBranchName() != null ? request.getBranchName() : "Main Branch")
                .build();

        account = accountRepository.save(account);
        log.info("Account created: {} for user: {}", accountNumber, userId);

        publishEvent("ACCOUNT_CREATED", Map.of(
                "accountNumber", accountNumber,
                "userId", userId,
                "accountType", request.getAccountType().name()
        ));

        return mapToDto(account);
    }

    @Override
    public List<AccountDto> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream().map(this::mapToDto).toList();
    }

    @Override
    public AccountDto getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        return mapToDto(account);
    }

    @Override
    public AccountDto getAccountById(Long id) {
        return mapToDto(accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id)));
    }

    @Override
    @Transactional
    public AccountDto updateBalance(String accountNumber, BigDecimal newBalance) {
        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        account.setBalance(newBalance);
        return mapToDto(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountDto freezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        account.setStatus(AccountStatus.FROZEN);
        return mapToDto(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountDto unfreezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        account.setStatus(AccountStatus.ACTIVE);
        return mapToDto(accountRepository.save(account));
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll().stream().map(this::mapToDto).toList();
    }

    // --- Beneficiary ---

    @Override
    @Transactional
    public Beneficiary addBeneficiary(Long userId, AddBeneficiaryRequest request) {
        if (beneficiaryRepository.existsByUserIdAndAccountNumber(userId, request.getAccountNumber())) {
            throw new BadRequestException("Beneficiary with this account number already exists");
        }
        Beneficiary b = Beneficiary.builder()
                .userId(userId)
                .name(request.getName())
                .accountNumber(request.getAccountNumber())
                .bankName(request.getBankName())
                .ifscCode(request.getIfscCode())
                .nickname(request.getNickname())
                .build();
        return beneficiaryRepository.save(b);
    }

    @Override
    public List<Beneficiary> getBeneficiaries(Long userId) {
        return beneficiaryRepository.findByUserIdAndActiveTrue(userId);
    }

    @Override
    @Transactional
    public void deleteBeneficiary(Long userId, Long beneficiaryId) {
        Beneficiary b = beneficiaryRepository.findByIdAndUserId(beneficiaryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary", "id", beneficiaryId));
        b.setActive(false);
        beneficiaryRepository.save(b);
    }

    private void publishEvent(String event, Map<String, Object> data) {
        try {
            StringBuilder sb = new StringBuilder("{\"event\":\"").append(event).append("\"");
            data.forEach((k, v) -> sb.append(",\"").append(k).append("\":\"").append(v).append("\""));
            sb.append("}");
            kafkaTemplate.send("account-events", sb.toString());
        } catch (Exception e) {
            log.warn("Failed to publish Kafka event: {}", e.getMessage());
        }
    }

    private AccountDto mapToDto(Account a) {
        return AccountDto.builder()
                .id(a.getId())
                .accountNumber(a.getAccountNumber())
                .userId(a.getUserId())
                .userEmail(a.getUserEmail())
                .accountHolderName(a.getAccountHolderName())
                .accountType(a.getAccountType())
                .status(a.getStatus())
                .balance(a.getBalance())
                .currency(a.getCurrency())
                .ifscCode(a.getIfscCode())
                .branchName(a.getBranchName())
                .dailyTransferLimit(a.getDailyTransferLimit())
                .transferredToday(a.getTransferredToday())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
