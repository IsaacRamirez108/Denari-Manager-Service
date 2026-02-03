package com.denari.manager.models.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VirtualAccountResponse {
    private Long id;
    private String name;
    private Boolean liveMode;
    private LocalDateTime createdAt;
    private String description;
    private List<AccountDetailResponse> accountDetails;
    private List<RoutingDetailResponse> routingDetails;
}
