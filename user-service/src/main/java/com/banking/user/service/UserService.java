package com.banking.user.service;

import com.banking.user.dto.UpdateProfileRequest;
import com.banking.user.dto.UserProfileDto;
import com.banking.user.enums.KycStatus;

import java.util.List;

public interface UserService {
    UserProfileDto getProfile(Long authUserId);
    UserProfileDto getProfileByEmail(String email);
    UserProfileDto createProfile(Long authUserId, String email, String fullName, String phone);
    UserProfileDto updateProfile(Long authUserId, UpdateProfileRequest request);
    List<UserProfileDto> getAllUsers();
    UserProfileDto updateKycStatus(Long userId, KycStatus status);
    UserProfileDto toggleAccountLock(Long userId, boolean lock);
}
