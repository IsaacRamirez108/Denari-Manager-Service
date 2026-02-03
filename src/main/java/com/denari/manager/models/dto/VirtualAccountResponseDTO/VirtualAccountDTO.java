package com.denari.manager.models.dto.VirtualAccountResponseDTO;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountDTO {
    private String id;
    private String virtual_account_id;
    private String counterparty_id;
    private String ledger_account_id;
    private String internal_account_id;
    private String debit_ledger_account_id;
    private String credit_ledger_account_id;
    private boolean active;
    private String name;
    private String object;
    private Object metadata;
    private boolean live_mode;
    private OffsetDateTime created_at; // Use OffsetDateTime for ZonedDateTime
    private OffsetDateTime updated_at;
    private String description;
    private String discarded_at;
    private List<AccountDetailDTO> account_details;
    private List<RoutingDetailDTO> routing_details;
}
