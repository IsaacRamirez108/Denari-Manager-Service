package com.denari.manager.models.entity.User;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "onboarding_statuses")
@Data
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class OnboardingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false)
    private OnboardingStep currentStep;

    @ElementCollection(targetClass = OnboardingStep.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "completed_onboarding_steps", joinColumns = @JoinColumn(name = "onboarding_status_id"))
    @Column(name = "step")
    private Set<OnboardingStep> completedSteps;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id") // ✅ FIXED
    private User user;


    public enum OnboardingStep {
        PHONE_VERIFICATION,
        PERSONAL_INFO,
        ADDRESS,
        RENTAL_DATA,
        PROPERTY_MANAGER,
        IDENTITY_VERIFICATION,
        PAYMENT_SCHEDULE,
        BANK_CONNECTION,
        VIRTUAL_ACCOUNT_SETUP,
        COMPLETED
    }
}
