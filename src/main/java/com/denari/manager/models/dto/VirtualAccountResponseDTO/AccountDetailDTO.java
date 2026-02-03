package com.denari.manager.models.dto.VirtualAccountResponseDTO;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDetailDTO {
    private String account_detail_id;
    private String object;
    private boolean live_mode;
    private OffsetDateTime created_at;
    private OffsetDateTime updated_at;
    private String discarded_at;
    private String account_number_safe;
    private String account_number_type;
}
