package com.denari.manager.repositories;

import com.denari.manager.models.entity.User.RentalData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalDataRepository extends JpaRepository<RentalData, Long> {
}
