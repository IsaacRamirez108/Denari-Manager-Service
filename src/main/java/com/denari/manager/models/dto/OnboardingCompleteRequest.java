package com.denari.manager.models.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Data
public class OnboardingCompleteRequest {
    @Valid
    @NotNull
    private UserRequest user;

    @Valid
    @NotNull
    private UserIdentityRequest userIdentity;

    @Valid
    @NotNull
    private RentalDataRequest rentalData;

    @Valid
    @NotNull
    private AddressRequest address;

    @Valid
    @NotNull
    private PropertyManagerInfoRequest propertyManagerInfo;

    @Valid
    @NotNull
    private PaymentScheduleRequest paymentSchedule;

    @Valid
    @NotNull
    private ExternalBankAccountRequest externalBankAccount;
}

