package com.denari.manager.repositories;

import com.denari.manager.models.entity.VirtualAccount.BankAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAddressRepository extends JpaRepository<BankAddress, String> {
}
