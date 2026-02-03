package com.denari.manager.repositories;

import com.denari.manager.models.entity.Webhook.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, String> {

}
