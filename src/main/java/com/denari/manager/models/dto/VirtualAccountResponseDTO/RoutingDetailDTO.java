package com.denari.manager.models.dto.VirtualAccountResponseDTO;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingDetailDTO {
    private String routing_detail_id;
    private String object;
    private String bank_name;
    private boolean live_mode;
    private OffsetDateTime created_at;
    private OffsetDateTime updated_at;
    private AddressDTO bank_address;
    private String discarded_at;
    private String payment_type;
    private String routing_number;
    private String routing_number_type;
}
