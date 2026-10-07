package com.trainbooking.repository;

import com.trainbooking.entity.Booking;
import com.trainbooking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBooking(Booking booking);

    Optional<Payment> findByTransactionId(String transactionId);
}