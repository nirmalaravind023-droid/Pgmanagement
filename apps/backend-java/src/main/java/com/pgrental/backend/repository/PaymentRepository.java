package com.pgrental.backend.repository;

import com.pgrental.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByUserId(String userId);
    List<Payment> findByBookingId(String bookingId);
}
