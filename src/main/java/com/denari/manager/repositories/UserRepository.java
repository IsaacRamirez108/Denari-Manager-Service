package com.denari.manager.repositories;

import com.denari.manager.models.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    Optional<User> findByUserIdentity_PhoneNumber(String phoneNumber); // For OTP login
}
