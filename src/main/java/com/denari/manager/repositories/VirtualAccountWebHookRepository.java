package com.denari.manager.repositories;

import com.denari.manager.models.entity.Webhook.VirtualAccountWebHook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualAccountWebHookRepository extends JpaRepository<VirtualAccountWebHook, String> {
}

