package com.denari.manager.models.entity.Webhook;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "virtual_account_webhook")
public class VirtualAccountWebHook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "virtual_account_webhook_id")
    private long id;
    @Column(name = "virtual_account_webhook_external_id")
    private String externalId;
    @Column(nullable = false)
    private String event;

}
