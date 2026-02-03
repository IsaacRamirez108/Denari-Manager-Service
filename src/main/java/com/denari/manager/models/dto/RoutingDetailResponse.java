package com.denari.manager.models.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoutingDetailResponse {
    private Long id;
    private String bankName;
    private Boolean liveMode;
    private LocalDateTime createdAt;
    private String paymentType;
    private String routingNumber;
    private String routingNumberType;
    private BankAddressResponse bankAddress;
}
