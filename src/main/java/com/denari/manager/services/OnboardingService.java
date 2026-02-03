package com.denari.manager.services;

import com.denari.manager.models.dto.OnboardingSummaryResponse;
import com.denari.manager.models.entity.User.*;
import com.denari.manager.enums.PaymentScheduleStatus;
import com.denari.manager.models.entity.VirtualAccount.VirtualAccount;
import com.denari.manager.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

@Service
@Slf4j
public class OnboardingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private OnboardingStatusRepository onboardingStatusRepository;

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Autowired
    private RentalDataRepository rentalDataRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private PropertyManagerInfoRepository propertyManagerInfoRepository;

    @Autowired
    private PaymentScheduleRepository paymentScheduleRepository;

    @Autowired
    private VirtualAccountRepository virtualAccountRepository;

    @Autowired
    private ModernTreasuryService modernTreasuryService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Update personal information and progress to ADDRESS step
     */
    @Transactional
    public OnboardingStatus.OnboardingStep updatePersonalInfo(Long userId, Map<String, String> personalInfo) {
        try {
            User user = getUserById(userId);

            // Update user basic info
            user.setFirstName(personalInfo.get("firstName"));
            user.setLastName(personalInfo.get("lastName"));
            user.setEmail(personalInfo.get("email"));
            userRepository.save(user);

            // Progress onboarding
            OnboardingStatus.OnboardingStep nextStep = OnboardingStatus.OnboardingStep.ADDRESS;
            updateOnboardingProgress(userId, nextStep, OnboardingStatus.OnboardingStep.PERSONAL_INFO);

            log.info("Personal info updated for user: {}", userId);
            return nextStep;

        } catch (Exception e) {
            log.error("Error updating personal info for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update personal information");
        }
    }

    /**
     * Update address information and progress to RENTAL_DATA step
     */
    @Transactional
    public OnboardingStatus.OnboardingStep updateAddress(Long userId, Map<String, String> addressInfo) {
        try {
            log.info("🏠 SERVICE: Starting updateAddress for user: {}", userId);
            log.info("🏠 SERVICE: Address info received: {}", addressInfo);

            // Get user
            log.info("🏠 SERVICE: Getting user by ID: {}", userId);
            User user = getUserById(userId);
            log.info("🏠 SERVICE: Found user: {} {}", user.getFirstName(), user.getLastName());

            // Get or create rental data
            log.info("🏠 SERVICE: Getting existing rental data...");
            RentalData rentalData = user.getRentalData();

            if (rentalData == null) {
                log.info("🏠 SERVICE: No existing rental data, creating new one...");
                rentalData = new RentalData();
                rentalData.setUser(user);
                rentalData.setFormPayment(RentalData.FormPayment.ACH);
                rentalData.setCurrency("USD");
                rentalData.setRentDueDate(1); // Default to 1st of month

                // ✅ FIX: Set default values for required NOT NULL fields
                rentalData.setMonthlyRentCents(0L); // Default to 0, will be updated in rental-data step
                log.info("🏠 SERVICE: Set default values - monthlyRentCents: 0, formPayment: ACH, currency: USD, rentDueDate: 1");

                log.info("🏠 SERVICE: Saving new rental data...");
                rentalData = rentalDataRepository.save(rentalData);
                log.info("🏠 SERVICE: Rental data saved with ID: {}", rentalData.getId());
            } else {
                log.info("🏠 SERVICE: Found existing rental data with ID: {}", rentalData.getId());
            }

            // Create or update address
            log.info("🏠 SERVICE: Getting existing user address...");
            UserAddress address = rentalData.getUserAddress();

            if (address == null) {
                log.info("🏠 SERVICE: No existing address, creating new one...");
                address = new UserAddress();
                address.setRentalData(rentalData);
                log.info("🏠 SERVICE: Created new UserAddress and linked to rental data");
            } else {
                log.info("🏠 SERVICE: Found existing address with ID: {}", address.getId());
            }

            // Set address fields
            log.info("🏠 SERVICE: Setting address fields...");
            String street = addressInfo.get("street");
            String city = addressInfo.get("city");
            String state = addressInfo.get("state");
            String postalCode = addressInfo.get("postalCode");
            String apartmentNumber = addressInfo.get("apartmentNumber");

            log.info("🏠 SERVICE: Street: '{}'", street);
            log.info("🏠 SERVICE: City: '{}'", city);
            log.info("🏠 SERVICE: State: '{}'", state);
            log.info("🏠 SERVICE: PostalCode: '{}'", postalCode);
            log.info("🏠 SERVICE: ApartmentNumber: '{}'", apartmentNumber);

            address.setStreet(street);
            address.setCity(city);
            address.setState(state);
            address.setPostalCode(postalCode);
            address.setApartmentNumber(apartmentNumber);
            address.setCountry("US");

            log.info("🏠 SERVICE: All address fields set, saving address...");
            UserAddress savedAddress = userAddressRepository.save(address);
            log.info("🏠 SERVICE: Address saved successfully with ID: {}", savedAddress.getId());

            // Progress onboarding
            log.info("🏠 SERVICE: Updating onboarding progress...");
            OnboardingStatus.OnboardingStep nextStep = OnboardingStatus.OnboardingStep.RENTAL_DATA;
            updateOnboardingProgress(userId, nextStep, OnboardingStatus.OnboardingStep.ADDRESS);
            log.info("🏠 SERVICE: Onboarding progress updated to: {}", nextStep);

            log.info("✅ SERVICE: Address update completed successfully for user: {}", userId);
            return nextStep;

        } catch (Exception e) {
            log.error("❌ SERVICE: Error updating address for user {}: {}", userId, e.getMessage(), e);
            log.error("❌ SERVICE: Exception type: {}", e.getClass().getSimpleName());
            log.error("❌ SERVICE: Exception cause: {}", e.getCause() != null ? e.getCause().getMessage() : "No cause");
            throw new RuntimeException("Failed to update address information: " + e.getMessage(), e);
        }
    }

    /**
     * Update rental data and progress to PROPERTY_MANAGER step
     */
    @Transactional
    public OnboardingStatus.OnboardingStep updateRentalData(Long userId, Map<String, String> rentalInfo) {
        try {
            User user = getUserById(userId);
            RentalData rentalData = user.getRentalData();

            if (rentalData == null) {
                throw new RuntimeException("Rental data not found. Please complete address step first.");
            }

            // Parse and set rental data
            String monthlyRentStr = rentalInfo.get("monthlyRent").replaceAll("[^\\d.]", "");
            Double monthlyRent = Double.parseDouble(monthlyRentStr);
            rentalData.setMonthlyRentCents(Math.round(monthlyRent * 100));

            // Parse dates
            LocalDate moveInDate = LocalDate.parse(rentalInfo.get("moveInDate"), dateFormatter);
            LocalDate moveOutDate = LocalDate.parse(rentalInfo.get("moveOutDate"), dateFormatter);
            rentalData.setMoveInDate(moveInDate);
            rentalData.setMoveOutDate(moveOutDate);

            rentalDataRepository.save(rentalData);

            // Progress onboarding
            OnboardingStatus.OnboardingStep nextStep = OnboardingStatus.OnboardingStep.PROPERTY_MANAGER;
            updateOnboardingProgress(userId, nextStep, OnboardingStatus.OnboardingStep.RENTAL_DATA);

            log.info("Rental data updated for user: {}", userId);
            return nextStep;

        } catch (Exception e) {
            log.error("Error updating rental data for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update rental information");
        }
    }

    /**
     * Update property manager info and progress to IDENTITY_VERIFICATION step
     */
    @Transactional
    public OnboardingStatus.OnboardingStep updatePropertyManager(Long userId, Map<String, String> managerInfo) {
        try {
            log.info("🏢 SERVICE: Starting updatePropertyManager for user: {}", userId);
            log.info("🏢 SERVICE: Manager info received: {}", managerInfo);

            User user = getUserById(userId);
            RentalData rentalData = user.getRentalData();

            if (rentalData == null) {
                throw new RuntimeException("Rental data not found. Please complete previous steps first.");
            }

            // Create or update property manager info
            PropertyManagerInfo pmInfo = rentalData.getPropertyManagerInfo();
            if (pmInfo == null) {
                pmInfo = new PropertyManagerInfo();
                pmInfo.setRentalData(rentalData);
            }

            // ✅ SIMPLE: Use frontend field names directly
            pmInfo.setName(managerInfo.get("managerName"));
            pmInfo.setEmail(managerInfo.get("managerEmail"));
            pmInfo.setPhoneNumber(managerInfo.get("managerPhone"));
            pmInfo.setIndividualOrCompany(PropertyManagerInfo.ManagerType.COMPANY);

            propertyManagerInfoRepository.save(pmInfo);

            // Progress onboarding
            OnboardingStatus.OnboardingStep nextStep = OnboardingStatus.OnboardingStep.IDENTITY_VERIFICATION;
            updateOnboardingProgress(userId, nextStep, OnboardingStatus.OnboardingStep.PROPERTY_MANAGER);

            log.info("✅ SERVICE: Property manager updated successfully for user: {}", userId);
            return nextStep;

        } catch (Exception e) {
            log.error("❌ SERVICE: Error updating property manager: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update property manager information", e);
        }
    }

    /**
     * Update identity verification and progress to PAYMENT_SCHEDULE step - PRODUCTION READY
     */
    @Transactional
    public OnboardingStatus.OnboardingStep updateIdentityVerification(Long userId, Map<String, String> identityInfo) {
        try {
            User user = getUserById(userId);
            UserIdentity userIdentity = user.getUserIdentity();

            if (userIdentity == null) {
                throw new RuntimeException("User identity not found.");
            }

            // Parse and validate date of birth
            try {
                LocalDate dob = LocalDate.parse(identityInfo.get("dob"), dateFormatter);
                userIdentity.setDateOfBirth(dob);
                log.info("Date of birth parsed successfully: {}", dob);
            } catch (Exception e) {
                log.error("Error parsing date of birth '{}': {}", identityInfo.get("dob"), e.getMessage());
                throw new RuntimeException("Invalid date format. Please use MM/dd/yyyy format.");
            }

            // Process SSN: Create both encrypted and masked versions
            String ssn = identityInfo.get("ssn");
            if (!encryptionService.isValidSSN(ssn)) {
                throw new RuntimeException("Invalid SSN format");
            }

            String encryptedSSN = encryptionService.encryptSSN(ssn);
            userIdentity.setSsnEncrypted(encryptedSSN);

            String maskedSSN = encryptionService.createMaskedSSN(ssn);
            userIdentity.setSsnMasked(maskedSSN);

            log.info("SSN encrypted and masked for user: {} - Masked: {}", userId, maskedSSN);

            // Save the userIdentity entity
            userIdentityRepository.save(userIdentity);
            log.info("UserIdentity saved successfully for user: {}", userId);

            // ✅ PRODUCTION: Create Modern Treasury counterparty and virtual account
            try {
                log.info("Creating Modern Treasury accounts for user: {}", userId);

                // Step 1: Create counterparty
                String counterpartyId = modernTreasuryService.createCounterparty(user);
                log.info("Counterparty created: {}", counterpartyId);

                // Step 2: Create virtual account
                String virtualAccountId = modernTreasuryService.createVirtualAccount(user, counterpartyId);
                log.info("Virtual account created: {}", virtualAccountId);

                log.info("Modern Treasury accounts created successfully for user: {}", userId);

            } catch (Exception e) {
                log.error("Modern Treasury account creation failed for user {}: {}", userId, e.getMessage(), e);

                // ✅ PRODUCTION: Don't fail onboarding, but log the error
                // The user can retry bank connection later if needed
                log.warn("Continuing onboarding without Modern Treasury accounts for user: {}", userId);
            }

            // Progress onboarding
            OnboardingStatus.OnboardingStep nextStep = OnboardingStatus.OnboardingStep.PAYMENT_SCHEDULE;
            updateOnboardingProgress(userId, nextStep, OnboardingStatus.OnboardingStep.IDENTITY_VERIFICATION);

            log.info("Identity verification updated for user: {}", userId);
            return nextStep;

        } catch (Exception e) {
            log.error("Error updating identity verification for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update identity verification: " + e.getMessage());
        }
    }

    /**
     *  Update payment schedule and progress to BANK_CONNECTION step
     */
    @Transactional
    public OnboardingStatus.OnboardingStep updatePaymentSchedule(Long userId, Map<String, String> scheduleInfo) {
        try {
            log.info("💰 SERVICE: Starting updatePaymentSchedule for user: {}", userId);

            User user = getUserById(userId);
            RentalData rentalData = user.getRentalData();

            if (rentalData == null) {
                throw new RuntimeException("Rental data not found. Please complete previous steps first.");
            }

            // ✅ Convert monthly rent from cents to BigDecimal
            BigDecimal monthlyRent = new BigDecimal(rentalData.getMonthlyRentCents()).divide(new BigDecimal("100"));
            log.info("💰 Monthly rent from database: ${}", monthlyRent);

            // Create or update payment schedule
            PaymentSchedule schedule = rentalData.getPaymentSchedule();
            if (schedule == null) {
                log.info("💰 Creating new payment schedule");
                schedule = new PaymentSchedule();
                schedule.setRentalData(rentalData);
            } else {
                log.info("💰 Updating existing payment schedule with ID: {}", schedule.getId());
            }

            // Set payment dates
            Integer secondPaymentDate = Integer.parseInt(scheduleInfo.get("secondPaymentDate"));
            schedule.setFirstPaymentDate(1); // Always 1st of month
            schedule.setSecondPaymentDate(secondPaymentDate);

            // ✅ Calculate payment amounts using helper method
            schedule.calculatePayments(monthlyRent);

            log.info("💰 Payment calculations:");
            log.info("💰 - First payment: ${}", schedule.getFirstPaymentAmount());
            log.info("💰 - Second payment: ${}", schedule.getSecondPaymentAmount());
            log.info("💰 - Service fee: ${}", schedule.getServiceFee());
            log.info("💰 - Total monthly charge: ${}", schedule.getTotalMonthlyCharge());

            schedule.setStatus(PaymentScheduleStatus.ACTIVE);

            PaymentSchedule savedSchedule = paymentScheduleRepository.save(schedule);
            log.info("💰 Payment schedule saved with ID: {}", savedSchedule.getId());

            // Progress onboarding
            OnboardingStatus.OnboardingStep nextStep = OnboardingStatus.OnboardingStep.BANK_CONNECTION;
            updateOnboardingProgress(userId, nextStep, OnboardingStatus.OnboardingStep.PAYMENT_SCHEDULE);

            log.info("✅ Payment schedule updated for user: {}", userId);
            return nextStep;

        } catch (Exception e) {
            log.error("❌ Error updating payment schedule for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update payment schedule: " + e.getMessage());
        }
    }

    /**
     * Complete virtual account setup and finish onboarding
     */
    @Transactional
    public OnboardingStatus.OnboardingStep completeVirtualAccountSetup(Long userId) {
        try {
            // Mark onboarding as completed
            OnboardingStatus.OnboardingStep nextStep = OnboardingStatus.OnboardingStep.COMPLETED;
            updateOnboardingProgress(userId, nextStep, OnboardingStatus.OnboardingStep.VIRTUAL_ACCOUNT_SETUP);

            // Update user status to ACTIVE
            User user = getUserById(userId);
            user.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(user);

            log.info("Onboarding completed for user: {}", userId);
            return nextStep;

        } catch (Exception e) {
            log.error("Error completing onboarding for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to complete onboarding");
        }
    }

    /**
     * ✅ NEW: Get onboarding summary for user
     */
    public OnboardingSummaryResponse getOnboardingSummary(Long userId) {
        try {
            log.info("📋 Getting onboarding summary for user: {}", userId);

            User user = getUserById(userId);
            OnboardingSummaryResponse summary = new OnboardingSummaryResponse();

            // User information
            OnboardingSummaryResponse.UserSummary userSummary = new OnboardingSummaryResponse.UserSummary();
            userSummary.setFirstName(user.getFirstName());
            userSummary.setLastName(user.getLastName());
            userSummary.setEmail(user.getEmail());
            if (user.getUserIdentity() != null) {
                userSummary.setPhoneNumber(user.getUserIdentity().getPhoneNumber());
            }
            summary.setUser(userSummary);

            // Address information
            if (user.getRentalData() != null && user.getRentalData().getUserAddress() != null) {
                UserAddress address = user.getRentalData().getUserAddress();
                OnboardingSummaryResponse.AddressSummary addressSummary = new OnboardingSummaryResponse.AddressSummary();
                addressSummary.setStreet(address.getStreet());
                addressSummary.setApartmentNumber(address.getApartmentNumber());
                addressSummary.setCity(address.getCity());
                addressSummary.setState(address.getState());
                addressSummary.setPostalCode(address.getPostalCode());

                // Build full address
                String fullAddress = address.getStreet();
                if (address.getApartmentNumber() != null && !address.getApartmentNumber().isEmpty()) {
                    fullAddress += ", " + address.getApartmentNumber();
                }
                fullAddress += ", " + address.getCity() + ", " + address.getState() + " " + address.getPostalCode();
                addressSummary.setFullAddress(fullAddress);

                summary.setAddress(addressSummary);
            }

            // Rental information
            if (user.getRentalData() != null) {
                RentalData rentalData = user.getRentalData();
                OnboardingSummaryResponse.RentalSummary rentalSummary = new OnboardingSummaryResponse.RentalSummary();

                // Convert cents to BigDecimal
                BigDecimal monthlyRent = new BigDecimal(rentalData.getMonthlyRentCents()).divide(new BigDecimal("100"));
                rentalSummary.setMonthlyRent(monthlyRent);
                rentalSummary.setCurrency(rentalData.getCurrency());

                if (rentalData.getMoveInDate() != null) {
                    rentalSummary.setMoveInDate(rentalData.getMoveInDate().format(dateFormatter));
                }
                if (rentalData.getMoveOutDate() != null) {
                    rentalSummary.setMoveOutDate(rentalData.getMoveOutDate().format(dateFormatter));
                }

                summary.setRental(rentalSummary);

                // Property Manager information
                if (rentalData.getPropertyManagerInfo() != null) {
                    PropertyManagerInfo pmInfo = rentalData.getPropertyManagerInfo();
                    OnboardingSummaryResponse.PropertyManagerSummary pmSummary = new OnboardingSummaryResponse.PropertyManagerSummary();
                    pmSummary.setName(pmInfo.getName());
                    pmSummary.setEmail(pmInfo.getEmail());
                    pmSummary.setPhoneNumber(pmInfo.getPhoneNumber());
                    pmSummary.setPropertyName(pmInfo.getPropertyName());
                    summary.setPropertyManager(pmSummary);
                }

                // ✅ Payment Schedule information (from saved PaymentSchedule entity)
                if (rentalData.getPaymentSchedule() != null) {
                    PaymentSchedule paymentSchedule = rentalData.getPaymentSchedule();
                    OnboardingSummaryResponse.PaymentScheduleSummary psSummary = new OnboardingSummaryResponse.PaymentScheduleSummary();
                    psSummary.setFirstPaymentDate(paymentSchedule.getFirstPaymentDate());
                    psSummary.setSecondPaymentDate(paymentSchedule.getSecondPaymentDate());
                    psSummary.setFirstPaymentAmount(paymentSchedule.getFirstPaymentAmount());
                    psSummary.setSecondPaymentAmount(paymentSchedule.getSecondPaymentAmount());
                    psSummary.setServiceFee(paymentSchedule.getServiceFee());
                    psSummary.setTotalMonthlyCharge(paymentSchedule.getTotalMonthlyCharge());
                    psSummary.setMonthlyRent(monthlyRent); // Base rent without service fee
                    summary.setPaymentSchedule(psSummary);

                    log.info("📋 Payment schedule found - Total charge: ${}", paymentSchedule.getTotalMonthlyCharge());
                } else {
                    log.warn("📋 No payment schedule found for user: {}", userId);
                }
            }

            // Bank Account information (if available)
            // Note: You might want to add this later when bank accounts are connected

            // Virtual Account information (if available)
            Optional<VirtualAccount> virtualAccountOpt = virtualAccountRepository.findByUserId(userId);
            if (virtualAccountOpt.isPresent()) {
                VirtualAccount virtualAccount = virtualAccountOpt.get();
                OnboardingSummaryResponse.VirtualAccountSummary vaSummary = new OnboardingSummaryResponse.VirtualAccountSummary();
                vaSummary.setAccountName(virtualAccount.getName());
                vaSummary.setActive(virtualAccount.getActive());

                // Get account details
                if (virtualAccount.getAccountDetails() != null && !virtualAccount.getAccountDetails().isEmpty()) {
                    vaSummary.setAccountNumberSafe(virtualAccount.getAccountDetails().get(0).getAccountNumberSafe());
                }

                // Get routing details
                if (virtualAccount.getRoutingDetails() != null && !virtualAccount.getRoutingDetails().isEmpty()) {
                    vaSummary.setRoutingNumber(virtualAccount.getRoutingDetails().get(0).getRoutingNumber());
                    vaSummary.setBankName(virtualAccount.getRoutingDetails().get(0).getBankName());
                }

                summary.setVirtualAccount(vaSummary);
            }

            log.info("✅ Onboarding summary compiled successfully for user: {}", userId);
            return summary;

        } catch (Exception e) {
            log.error("❌ Error getting onboarding summary for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to get onboarding summary: " + e.getMessage());
        }
    }


    /**
     * Get current onboarding status
     */
    public OnboardingStatus getCurrentOnboardingStatus(Long userId) {
        return onboardingStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Onboarding status not found for user: " + userId));
    }

    /**
     * Helper method to update onboarding progress
     */
    private void updateOnboardingProgress(Long userId, OnboardingStatus.OnboardingStep nextStep,
                                          OnboardingStatus.OnboardingStep completedStep) {
        OnboardingStatus status = getCurrentOnboardingStatus(userId);

        // Add completed step to the set
        Set<OnboardingStatus.OnboardingStep> completed = status.getCompletedSteps();
        if (completed == null) {
            completed = new HashSet<>();
        }
        completed.add(completedStep);
        status.setCompletedSteps(completed);

        // Update current step
        status.setCurrentStep(nextStep);

        // Mark as completed if final step
        if (nextStep == OnboardingStatus.OnboardingStep.COMPLETED) {
            status.setCompletedAt(LocalDateTime.now());
        }

        onboardingStatusRepository.save(status);
    }

    /**
     * Helper method to get user by ID
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    /**
     * Create Modern Treasury counterparty and virtual account
     */
    private void createModernTreasuryAccounts(User user) {
        try {
            // Create counterparty first
            String counterpartyId = modernTreasuryService.createCounterparty(user);

            // Create virtual account
            String virtualAccountId = modernTreasuryService.createVirtualAccount(user, counterpartyId);

            log.info("Modern Treasury accounts created for user {}: counterparty={}, virtualAccount={}",
                    user.getId(), counterpartyId, virtualAccountId);

        } catch (Exception e) {
            log.warn("Failed to create Modern Treasury accounts for user {}: {}", user.getId(), e.getMessage());
            // Don't fail onboarding if MT fails, we can retry later
        }
    }
}
