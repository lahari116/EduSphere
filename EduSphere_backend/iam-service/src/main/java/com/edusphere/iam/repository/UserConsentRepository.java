package com.edusphere.iam.repository;

import com.edusphere.iam.entity.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, UUID> {
    List<UserConsent> findByUserIdOrderByAcceptedAtDesc(UUID userId);
    boolean existsByUserIdAndTermsVersion(UUID userId, String version);
}
