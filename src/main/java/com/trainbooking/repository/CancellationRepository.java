package com.trainbooking.repository;

import com.trainbooking.entity.Booking;
import com.trainbooking.entity.Cancellation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CancellationRepository extends JpaRepository<Cancellation, Long> {

    Optional<Cancellation> findByBooking(Booking booking);

    boolean existsByBooking(Booking booking);
}