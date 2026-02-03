package com.denari.manager.repositories;

import com.denari.manager.models.entity.User.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
}
