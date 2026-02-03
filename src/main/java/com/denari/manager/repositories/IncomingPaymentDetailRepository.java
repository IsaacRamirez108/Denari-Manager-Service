package com.denari.manager.repositories;

import com.denari.manager.models.entity.Webhook.IncomingPaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomingPaymentDetailRepository extends JpaRepository<IncomingPaymentDetail, String> {

}
