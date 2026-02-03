package com.denari.manager.models.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RentalDataResponse {
    private Long id;
    private String formPayment;
    private BigDecimal monthlyRent; // Convert from cents for display
    private String currency;
    private LocalDate moveInDate;
    private LocalDate moveOutDate;
    private Integer rentDueDate;

    // Related data
    private PaymentScheduleResponse paymentSchedule;
    private AddressResponse address;
    private PropertyManagerInfoResponse propertyManagerInfo;
}

