package com.denari.manager.repositories;

import com.denari.manager.models.entity.User.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {
    Optional<UserIdentity> findByPhoneNumber(String phoneNumber);
    Optional<UserIdentity> findByPhoneNumberAndPhoneVerifiedTrue(String phoneNumber);
}
