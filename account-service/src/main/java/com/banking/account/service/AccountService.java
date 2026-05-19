package com.banking.account.service;

import com.banking.account.dto.AccountDto;
import com.banking.account.dto.AddBeneficiaryRequest;
import com.banking.account.dto.CreateAccountRequest;
import com.banking.account.entity.Beneficiary;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountDto createAccount(Long userId, String userEmail, String holderName, CreateAccountRequest request);
    List<AccountDto> getUserAccounts(Long userId);
    AccountDto getAccountByNumber(String accountNumber);
    AccountDto getAccountById(Long id);
    AccountDto updateBalance(String accountNumber, BigDecimal newBalance);
    AccountDto freezeAccount(Long accountId);
    AccountDto unfreezeAccount(Long accountId);
    List<AccountDto> getAllAccounts();

    Beneficiary addBeneficiary(Long userId, AddBeneficiaryRequest request);
    List<Beneficiary> getBeneficiaries(Long userId);
    void deleteBeneficiary(Long userId, Long beneficiaryId);
}
