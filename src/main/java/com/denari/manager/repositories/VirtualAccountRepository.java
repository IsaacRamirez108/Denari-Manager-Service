package com.denari.manager.repositories;

import com.denari.manager.models.entity.VirtualAccount.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, String> {
    Optional<VirtualAccount> findByUserId(Long userId);
    Optional<VirtualAccount> findByMtId(String mtId);
}

