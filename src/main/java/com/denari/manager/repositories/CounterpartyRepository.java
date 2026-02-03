package com.denari.manager.repositories;

import com.denari.manager.models.entity.ExternalAccount.Counterparty;
import com.denari.manager.models.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CounterpartyRepository extends JpaRepository<Counterparty, Long> {
    // ✅ REQUIRED: Find counterparty by user
    Optional<Counterparty> findByUser(User user);

    // ✅ REQUIRED: Find by Modern Treasury ID
    Optional<Counterparty> findByMtCounterpartyId(String mtCounterpartyId);

    // ✅ NEW: Find counterparty by user ID
    Optional<Counterparty> findByUser_Id(Long userId);
}

