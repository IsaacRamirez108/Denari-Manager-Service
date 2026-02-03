package com.denari.manager.repositories;

import com.denari.manager.models.entity.VirtualAccount.RoutingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutingDetailRepository extends JpaRepository<RoutingDetail, String> {
}

