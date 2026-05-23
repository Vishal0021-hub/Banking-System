package com.banking.user.repository;

import com.banking.user.entity.UserProfile;
import com.banking.user.enums.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByEmail(String email);
    Optional<UserProfile> findByAuthUserId(Long authUserId);
    List<UserProfile> findByKycStatus(KycStatus kycStatus);
    boolean existsByPanNumber(String panNumber);
    boolean existsByAadharNumber(String aadharNumber);
}
