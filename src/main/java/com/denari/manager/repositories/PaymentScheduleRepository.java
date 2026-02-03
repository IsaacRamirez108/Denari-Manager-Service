package com.denari.manager.repositories;

import com.denari.manager.models.entity.User.PaymentSchedule;
import com.denari.manager.models.entity.User.RentalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentScheduleRepository extends JpaRepository<PaymentSchedule, String> {
    Optional<PaymentSchedule> findByRentalData(RentalData rentalData);

    Optional<PaymentSchedule> findByRentalDataId(Long rentalDataId);

    Optional<PaymentSchedule> findByRentalData_UserId(Long userId);
}
