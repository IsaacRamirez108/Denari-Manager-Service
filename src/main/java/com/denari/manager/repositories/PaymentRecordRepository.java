package com.denari.manager.repositories;

import com.denari.manager.models.entity.Payments.PaymentRecord;
import com.denari.manager.models.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    // Find by Modern Treasury payment order ID
    Optional<PaymentRecord> findByMtPaymentOrderId(String mtPaymentOrderId);

    // Find by incoming payment ID (for idempotency checks)
    Optional<PaymentRecord> findByIncomingPaymentId(String incomingPaymentId);

    // Get all payments for a user
    List<PaymentRecord> findByUserOrderByEffectiveDateDesc(User user);

    // Get payments for a specific month
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.user = :user AND pr.rentMonth = :rentMonth")
    List<PaymentRecord> findByUserAndRentMonth(@Param("user") User user, @Param("rentMonth") String rentMonth);

    // Get pending payments
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.status IN ('SCHEDULED', 'PENDING')")
    List<PaymentRecord> findPendingPayments();

    // Get failed payments that need attention
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.status IN ('FAILED', 'RETURNED')")
    List<PaymentRecord> findFailedPayments();

    // Check if payment already exists for incoming payment ID (idempotency)
    boolean existsByIncomingPaymentId(String incomingPaymentId);

    // Get payments due today
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.effectiveDate = :date AND pr.status = 'SCHEDULED'")
    List<PaymentRecord> findPaymentsDueToday(@Param("date") LocalDate date);
}
