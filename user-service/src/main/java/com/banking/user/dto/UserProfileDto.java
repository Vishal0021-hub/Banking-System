package com.banking.user.dto;

import com.banking.user.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private Long authUserId;
    private String email;
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private String panNumber;
    private String aadharNumber;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private KycStatus kycStatus;
    private String profilePictureUrl;
    private boolean enabled;
    private boolean accountNonLocked;
    private LocalDateTime createdAt;
}
