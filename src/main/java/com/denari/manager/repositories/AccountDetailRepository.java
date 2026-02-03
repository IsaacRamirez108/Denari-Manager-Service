package com.denari.manager.repositories;

import com.denari.manager.models.entity.VirtualAccount.AccountDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountDetailRepository extends JpaRepository<AccountDetail, String> {
}
