package com.denari.manager.repositories;

import com.denari.manager.models.entity.User.OnboardingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingStatusRepository extends JpaRepository<OnboardingStatus, String> {
    Optional<OnboardingStatus> findByUserId(Long userId);
    List<OnboardingStatus> findByCurrentStepNot(OnboardingStatus.OnboardingStep step); // For finding incomplete onboardings
}
