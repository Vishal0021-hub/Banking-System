package com.banking.user.service.impl;

import com.banking.common.exception.BadRequestException;
import com.banking.common.exception.ResourceNotFoundException;
import com.banking.user.dto.UpdateProfileRequest;
import com.banking.user.dto.UserProfileDto;
import com.banking.user.entity.UserProfile;
import com.banking.user.enums.KycStatus;
import com.banking.user.repository.UserProfileRepository;
import com.banking.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public UserProfileDto getProfile(Long authUserId) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseGet(() -> {
                    log.warn("UserProfile not found for authUserId {}. Creating default profile.", authUserId);
                    UserProfile newProfile = UserProfile.builder()
                            .authUserId(authUserId)
                            .email("user" + authUserId + "@bank.com")
                            .fullName("Bank Customer")
                            .phone("0000000000")
                            .build();
                    return userProfileRepository.save(newProfile);
                });
        return mapToDto(profile);
    }

    @Override
    @Transactional
    public UserProfileDto getProfileOrAutoCreate(Long authUserId, String email, String fullName) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseGet(() -> {
                    log.info("Auto-initializing UserProfile for authUserId: {}, email: {}", authUserId, email);
                    UserProfile newProfile = UserProfile.builder()
                            .authUserId(authUserId)
                            .email(email != null && !email.isBlank() ? email : "user" + authUserId + "@bank.com")
                            .fullName(fullName != null && !fullName.isBlank() ? fullName : "Bank Customer")
                            .phone("0000000000")
                            .build();
                    return userProfileRepository.save(newProfile);
                });
        return mapToDto(profile);
    }

    @Override
    public UserProfileDto getProfileByEmail(String email) {
        UserProfile profile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "email", email));
        return mapToDto(profile);
    }

    @Override
    @Transactional
    public UserProfileDto createProfile(Long authUserId, String email, String fullName, String phone) {
        return createProfile(authUserId, email, fullName, phone, null, null);
    }

    @Override
    @Transactional
    public UserProfileDto createProfile(Long authUserId, String email, String fullName, String phone, String panNumber, String aadharNumber) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseGet(() -> UserProfile.builder()
                        .authUserId(authUserId)
                        .email(email)
                        .fullName(fullName)
                        .phone(phone)
                        .panNumber(panNumber)
                        .aadharNumber(aadharNumber)
                        .build());
        if (panNumber != null && !panNumber.isBlank()) {
            profile.setPanNumber(panNumber);
        }
        if (aadharNumber != null && !aadharNumber.isBlank()) {
            profile.setAadharNumber(aadharNumber);
        }
        profile = userProfileRepository.save(profile);
        return mapToDto(profile);
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(Long authUserId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "authUserId", authUserId));

        if (request.getPanNumber() != null && !request.getPanNumber().equals(profile.getPanNumber())) {
            if (userProfileRepository.existsByPanNumber(request.getPanNumber())) {
                throw new BadRequestException("PAN number already registered");
            }
            profile.setPanNumber(request.getPanNumber());
            profile.setKycStatus(KycStatus.UNDER_REVIEW);
        }
        if (request.getAadharNumber() != null && !request.getAadharNumber().equals(profile.getAadharNumber())) {
            if (userProfileRepository.existsByAadharNumber(request.getAadharNumber())) {
                throw new BadRequestException("Aadhar number already registered");
            }
            profile.setAadharNumber(request.getAadharNumber());
        }
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getCity() != null) profile.setCity(request.getCity());
        if (request.getState() != null) profile.setState(request.getState());
        if (request.getPincode() != null) profile.setPincode(request.getPincode());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());

        profile = userProfileRepository.save(profile);
        return mapToDto(profile);
    }

    @Override
    public List<UserProfileDto> getAllUsers() {
        return userProfileRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional
    public UserProfileDto updateKycStatus(Long userId, KycStatus status) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", userId));
        profile.setKycStatus(status);
        if (status == KycStatus.APPROVED) {
            profile.setKycApprovedAt(java.time.LocalDateTime.now());
        }
        return mapToDto(userProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public UserProfileDto toggleAccountLock(Long userId, boolean lock) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", userId));
        profile.setAccountNonLocked(!lock);
        return mapToDto(userProfileRepository.save(profile));
    }

    private UserProfileDto mapToDto(UserProfile profile) {
        return UserProfileDto.builder()
                .id(profile.getId())
                .authUserId(profile.getAuthUserId())
                .email(profile.getEmail())
                .fullName(profile.getFullName())
                .phone(profile.getPhone())
                .dateOfBirth(profile.getDateOfBirth())
                .panNumber(profile.getPanNumber())
                .aadharNumber(profile.getAadharNumber())
                .address(profile.getAddress())
                .city(profile.getCity())
                .state(profile.getState())
                .pincode(profile.getPincode())
                .kycStatus(profile.getKycStatus())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .enabled(profile.isEnabled())
                .accountNonLocked(profile.isAccountNonLocked())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
