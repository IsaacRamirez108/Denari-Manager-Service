package com.denari.manager.models.dto.VirtualAccountResponseDTO;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    private String address_id;
    private String line1;
    private String line2;
    private String object;
    private String region;
    private String country;
    private String locality;
    private boolean live_mode;
    private OffsetDateTime created_at;
    private OffsetDateTime updated_at;
    private String postal_code;
}
