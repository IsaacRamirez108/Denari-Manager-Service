package com.denari.manager.repositories;

import com.denari.manager.models.entity.User.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    List<PaymentHistory> findByUserIdAndStatusOrderByDueDateAsc(Long userId, PaymentHistory.PaymentStatus status);
    List<PaymentHistory> findByUserIdOrderByDueDateDesc(Long userId);
}

