package com.denari.manager.repositories;

import com.denari.manager.models.entity.ExternalAccount.ExternalBankAccount;
import com.denari.manager.models.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalBankAccountRepository extends JpaRepository<ExternalBankAccount, String> {
    Optional<ExternalBankAccount> findByMtExternalAccountId(String mtExternalAccountId);
    Optional<ExternalBankAccount> findByUserAndIsPrimaryTrue(User user);
    Optional<ExternalBankAccount> findByUser(User user);
}
